package com.maicard.mb.service.rabbitmq;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.maicard.mb.constants.MessageBusEnum;
import com.maicard.misc.EncryptPropertyPlaceholderConfigurer;
import com.maicard.utils.ClassUtils;
import com.maicard.utils.JsonUtils;
import com.maicard.utils.NumericUtils;
import com.maicard.utils.StringTools;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maicard.base.BaseService;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.DataName;
import com.maicard.core.constants.Operate;
import com.maicard.core.entity.BaseEntity;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.core.service.ConfigService;
import com.maicard.mb.annotation.ProcessMessageObject;
import com.maicard.mb.annotation.ProcessMessageOperate;
import com.maicard.mb.iface.EisMessageListener;
import com.maicard.mb.service.JmsDataSyncService;
import com.maicard.mb.service.MessageService;

/**
 * 基于RabbitMQ的全局消息消费者实现
 * 1、接收JMS队列中的消息
 * 2、调用容器中的其他服务的onMessage接口
 * 3、提供对队列的数据查询
 *
 * @author NetSnake
 * @date 2012-10-09
 */

@Service
public class MessageServiceImpl extends BaseService implements MessageService, MessageListener, ApplicationContextAware {

    @Resource
    private ApplicationContextService applicationContextService;
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private RabbitTemplate txRabbitTemplate;


    //不能自动注入
    private MessagePostProcessor eisMessagePostProcessor;
    @Resource
    protected EncryptPropertyPlaceholderConfigurer encryptPropertyPlaceholderConfigurer;


    private String jmsNodeId = null;

    private ApplicationContext applicationContext;


    private final ObjectMapper om = new ObjectMapper();

    private final SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_TIME_FORMAT);

    private final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(JsonUtils.getSerializeInstance());

    /**
     * 动作与处理该动作的所有bean的一个集合<br>
     * 当收到某个动作时，即调用对应Set中的所有bean处理具体业务
     */
    private static ConcurrentHashMap<String, Set<String>> handlerMap = null;
    boolean mqEnabled = false;

    @PostConstruct
    public void init() {

        mqEnabled = StringTools.isPositive(encryptPropertyPlaceholderConfigurer.getProperty(DataName.MQ_ENABLED.name()));
        if (mqEnabled && rabbitTemplate == null) {
            throw new RuntimeException("System MQ enabled, but MQ template not config");
        }
        om.setDateFormat(sdf);
        jmsNodeId = encryptPropertyPlaceholderConfigurer.getProperty(DataName.systemCode.name()) + "." + NumericUtils.parseInt(encryptPropertyPlaceholderConfigurer.getProperty(DataName.systemServerId.name()));

    }


    protected boolean getBoolProperty(String key) {
        String v = encryptPropertyPlaceholderConfigurer.getProperty(key);
        if (v != null && (v.trim().equalsIgnoreCase("true") || v.trim().equalsIgnoreCase("1"))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reply(String destination, String queue, String correlationId, Serializable m) {
		/*logger.debug("尝试回复消息到回复队列:" + queue + ",correlationId:" + correlationId);
		try{
			rabbitTemplate.setQueue(queue);
			//CorrelationData correlationData = new CorrelationData(correlationId);
			//rabbitTemplate.convertAndSend(null,m, correlationData);
			//rabbitTemplate.convertAndSend(null,m, eisMessagePostProcessor, correlationData);

			Message message = createMessage(m, new MessageProperties());
			message.getMessageProperties().setAppId(appId);
			message.getMessageProperties().setCorrelationId(correlationId);
			message.getMessageProperties().setMessageId(message.getMessageProperties().getAppId() + "-" + UUID.randomUUID().toString());
			//rabbitTemplate.(object, correlationData)
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}*/
    }

    protected Message createMessage(Object objectToConvert,
                                    MessageProperties messageProperties) {
        byte[] bytes = null;
        try {
            String jsonString = om.writeValueAsString(objectToConvert);
            bytes = jsonString.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessageConversionException(
                    "Failed to convert Message content", e);
        }
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding("UTF-8");
        if (bytes != null) {
            messageProperties.setContentLength(bytes.length);
        }
        return new Message(bytes, messageProperties);

    }

    @Override
    //@Async
    public String send(String destination, Serializable m) {
		/*if(StringUtils.isBlank(destination)){
			if(logger.isWarnEnabled()){
				logger.warn("消息目的地为空，停止发送");
			}
			return null;
		}*/
        if (!mqEnabled) {
            logger.debug("系统已关闭MQ功能");
            return null;
        }
        if (m == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("消息对象为空，停止发送");
            }
            return null;
        }
        if (eisMessagePostProcessor == null) {
            eisMessagePostProcessor = applicationContextService.getBeanGeneric("eisMessagePostProcessor");
        }
        if (destination != null && txRabbitTemplate != null && destination.equalsIgnoreCase(MessageBusEnum.TX.name())) {

            try {
                logger.debug("尝试发送消息至TX[" + destination + ",类型:" + m.getClass().getName() + "]");
                txRabbitTemplate.convertAndSend(m, eisMessagePostProcessor);
                return null;
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
        try {
            logger.debug("尝试发送消息至[" + destination + ",类型:" + m.getClass().getName() + "]");
            if (rabbitTemplate != null) rabbitTemplate.convertAndSend(m, eisMessagePostProcessor);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Async
    public Object sendAndReceive(String destination, Serializable m) {

        return null;
    }


    @Override
    @RabbitListener(queues = "EIS-QUEUE-${systemServerId}", concurrency = "1", ackMode = "AUTO")
    public void onMessage(Message message) {
        if (message == null) {
            return;
        }

        try {

            String correlationId = null;
            if (message.getMessageProperties().getCorrelationId() != null) {
                correlationId = new String(message.getMessageProperties().getCorrelationId());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("全局消息驱动处理器收到消息[messageId=" + message.getMessageProperties().getMessageId() + ",length=" + message.getBody().length + ",correlationId=" + correlationId + "]");
            }
            if (StringUtils.isBlank(message.getMessageProperties().getAppId()) && StringUtils.isBlank(message.getMessageProperties().getReplyTo())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("忽略未知APPID且不是需回复的消息[" + message.getMessageProperties().getMessageId() + "]");
                }
                return;
            }
            if (message.getMessageProperties().getAppId() != null && message.getMessageProperties().getAppId().equals(jmsNodeId)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("忽略本地消息[" + message.getMessageProperties().getMessageId() + "]");
                }
                return;
            }
            //byte[] correlationId = message.getMessageProperties().getCorrelationId();
			/*System.out.println("XX:correlationId=>" + message.getMessageProperties().getCorrelationId());
			System.out.println("XX:replyToAddress=>" + message.getMessageProperties().getReplyToAddress());
			for(String head : message.getMessageProperties().getHeaders().keySet()){
				System.out.println("XXXXXXXX>>>>" + head + "===>" + message.getMessageProperties().getHeaders().get(head));
			}*/

            Object object = null;
            try {
                object = converter.fromMessage(message);
            } catch (Exception e) {
                logger.error("无法解析消息:{},异常:{}", message, e.getMessage());
            }

            if (object == null) {
                return;
            }
			
			/*JsonMessageConverter converter = new JsonMessageConverter();
			Object object = converter.fromMessage(message);*/
            //Object object = om.readValue(message.getBody(),EisMessage.class);

            logger.debug("消息处理器收到的消息是:" + (object == null ? "空" : object.getClass().getName()));
            //converter.fromMessage(message);
            if (object instanceof EisMessage) {
                EisMessage eisMessage = (EisMessage) object;
                if (StringUtils.isNotBlank(message.getMessageProperties().getReplyTo())) {
                    logger.debug("[" + message.getMessageProperties().getMessageId() + "]是一条需要回复的消息,回复queue=" + message.getMessageProperties().getReplyTo() + ",receiveRoutingKey=" + message.getMessageProperties().getReceivedRoutingKey());
                    //eisMessage.setReceiverName(message.getMessageProperties().getReplyTo());
                    //eisMessage.setReplyMessageId(new String(message.getMessageProperties().getCorrelationId()));
                } else {
                    eisMessage.setMessageId(message.getMessageProperties().getMessageId());
                }
                eisMessage.setMessageId(message.getMessageProperties().getMessageId());

                operate(eisMessage);

            } else {
                logger.error("消息处理器收到的消息类型不是EisMessage，是:" + (object == null ? "空" : object.getClass().getName()) + ",无法处理");

            }
            object = null;
            //converter = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        message = null;

    }

    /*
     * 查找当前环境中标记为EisMessageListener接口的所有bean
     * 并提供了ListenEisMessage注解
     * 同时比对ListenEisMessage注解的值：
     * 1、如果objectType与消息中的objectType一致才是有效的接收者
     * 2、如果同时注解了operate，那么还要比较operate值是否与消息中的operate一致
     */
    private void operate(EisMessage eisMessage) throws Exception {
        long beginTime = new Date().getTime();

        /*
         * if(eisMessage.code == Operate.JmsDataSync.id){
         * jmsDataSyncService.operate(eisMessage); return; }
         */
        if (handlerMap == null) {
            initHandlerMap();
        }
        if (handlerMap == null || handlerMap.size() < 1) {
            logger.error("系统中没有类型为[" + EisMessageListener.class.getName() + "]的bean");
            return;
        }


        if (eisMessage.getObjectType() == null || eisMessage.getObjectType().equals("")) {
            logger.warn("消息[" + eisMessage.getMessageId() + "]未定义处理对象");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("处理消息[对象类型=" + eisMessage.getObjectType() + ",操作:" + eisMessage.getCode() + "]");
        }
        Set<String> objectTypeProcessorName = getProcessorName(eisMessage.getObjectType());
        if (objectTypeProcessorName == null || objectTypeProcessorName.size() < 1) {
            logger.warn("系统中没有处理对象[" + eisMessage.getObjectType() + "]的任何bean");
            return;
        }


        for (String beanName : objectTypeProcessorName) {
            if (logger.isDebugEnabled()) {
                logger.debug("尝试匹配服务[" + beanName + "]");
            }

            if (isProcessOperate(beanName, eisMessage.getCode()) == 0) {//定义了操作模式但不合法
                if (logger.isDebugEnabled()) {
                    logger.debug(beanName + "定义了操作模式但不相符");
                }
                continue;
            }
            logger.debug("1由[" + beanName + "]处理该消息");
            try {
                EisMessageListener processor = applicationContextService.getBeanGeneric(beanName);
                logger.debug("由[" + beanName + "]=>" + processor.getClass().getName() + "<===========>" + AopUtils.getTargetClass(processor) + ",处理该消息");
                processor.onMessage(eisMessage);
                break;
            } catch (Exception e) {
                logger.error("处理器:" + beanName + "处理消息时发生异常", e);
            }

        }


        if (logger.isDebugEnabled())
            logger.debug("消息[" + eisMessage.getMessageId() + "]处理完成,收到消息的时间是:" + sdf.format(beginTime) + ",处理耗时" + (new Date().getTime() - beginTime) + "毫秒");
        return;
    }

    private Set<String> getProcessorName(String objectType) {
        objectType = objectType.trim();
        Set<String> processorNameSet = new HashSet<String>();
        if (handlerMap.containsKey(objectType)) {
            processorNameSet.addAll(handlerMap.get(objectType));
        }
        if (handlerMap.containsKey("*")) {
            processorNameSet.addAll(handlerMap.get("*"));
        }

        return processorNameSet;
    }

    @Override
    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        this.applicationContext = arg0;
    }


    private int isProcessOperate(String beanName, int operateCode) {
        if (operateCode == 0) {
            return 2;
        }
        ProcessMessageOperate processOperateAnnotation = applicationContext.findAnnotationOnBean(beanName, ProcessMessageOperate.class);
        if (processOperateAnnotation != null) {
            if (processOperateAnnotation.value()[0].equals("*")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("bean[" + beanName + "]注解处理所有操作[*]");
                }
                return 1;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("检查bean[" + beanName + "]的处理操作");
                }
                for (String targetOperate : processOperateAnnotation.value()) {
                    boolean isValid = false;
                    for (Operate op : Operate.values()) {
                        if (op.name().toString().equals(targetOperate)) {
                            isValid = true;
                            break;
                        }
                    }
                    Operate op = Operate.findByCode(targetOperate);
                    if (op != null && operateCode == op.id && isValid) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("bean[" + beanName + "]注解处理[" + targetOperate + "]操作的对象");
                        }
                        return 1;
                    }


                }
            }
            return 0;
        } else {
            return 2;
        }

    }


    @Override
    public void sendJmsDataSyncMessage(String destination, String beanName,
                                       String methodName, Object... objects) {
        if (!mqEnabled) {
            logger.debug("系统已关闭MQ功能");
            return;
        }
        EisMessage syncMessage = new EisMessage();
        if (objects != null && objects.length > 0) {
            syncMessage.setObjectType(ClassUtils.getEntityType(objects[0].getClass()));
            for (Object object : objects) {
                if (object instanceof BaseEntity) {
                    BaseEntity jObject = (BaseEntity) object;
                    if (jObject.getSyncFlag() == 1) {
                        logger.warn("参数中有一个对象[" + object + "]的同步标记为1，停止发送同步消息");
                        return;
                    }
                    jObject.setSyncFlag(1);
                }
            }
        }
        syncMessage.setCode(Operate.JmsDataSync.id);
        syncMessage.setExtra("updateSlaveParamaters", objects);
        syncMessage.setExtra("updateSlaveBeanName", beanName);
        syncMessage.setExtra("updateSlaveMethodName", methodName);
        send(destination, syncMessage);
        syncMessage = null;

    }

    private synchronized void initHandlerMap() {
        ApplicationContext applicationContext = applicationContextService.getApplicationContext();

        Map<String, EisMessageListener> map = applicationContext.getBeansOfType(EisMessageListener.class);
        if (map == null || map.size() < 1) {
            logger.error("系统中没有类型为[" + EisMessageListener.class.getName() + "]的bean");
            return;
        }
        if (handlerMap == null) {
            handlerMap = new ConcurrentHashMap<String, Set<String>>();
        }
        for (String beanName : map.keySet()) {

            ProcessMessageObject processObjectAnnotation = applicationContext.findAnnotationOnBean(beanName, ProcessMessageObject.class);

            if (processObjectAnnotation == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug(beanName + "未声明ProcessWsMessageOperate注解");
                }
                continue;
            }
            if (processObjectAnnotation.value() == null || processObjectAnnotation.value().length < 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug(beanName + "的ProcessWsMessageOperate注解内容为空");
                }
                continue;
            }


            for (String value : processObjectAnnotation.value()) {
                if (StringUtils.isBlank(value)) {
                    logger.debug("忽略[" + beanName + "]类的注解ProcessMessageObject中的空指令");
                    continue;
                }
                if (handlerMap.get(value) == null) {
                    handlerMap.put(value, new HashSet<String>());
                }
                handlerMap.get(value).add(beanName);
                if (logger.isDebugEnabled()) {
                    logger.debug("把bean=" + beanName + "作为MDB对象[" + value + "]的处理者");
                }
            }

        }
        logger.info("MDB消息处理器缓存初始化完毕，系统共注册了" + map.size() + "个EisMessageListener处理器，当前有" + handlerMap.size() + "种处理对象");

    }


}
