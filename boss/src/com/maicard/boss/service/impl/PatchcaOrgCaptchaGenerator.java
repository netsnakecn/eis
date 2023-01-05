package com.maicard.boss.service.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.maicard.site.utils.ColorUtil;
import org.apache.commons.lang.StringUtils;
import org.patchca.background.BackgroundFactory;
import org.patchca.background.SingleColorBackgroundFactory;
import org.patchca.color.ColorFactory;
import org.patchca.color.SingleColorFactory;
import org.patchca.filter.FilterFactory;
import org.patchca.filter.predefined.CurvesRippleFilterFactory;
import org.patchca.font.RandomFontFactory;
import org.patchca.text.renderer.BestFitTextRenderer;
import org.patchca.text.renderer.TextRenderer;
import org.patchca.word.AdaptiveRandomWordFactory;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.site.entity.Captcha;
import com.maicard.site.iface.CaptchaGenerator;


/**
 * 使用org.patchca生成验证码
 * 
 * 2017.10.6
 * centos 7.3，nginx-1.10.2-1.el7.x86_64工作正常（与fontconfig-2.10.95-10.el7.x86_64配合）
 * 但是nginx-1.10.2-2.el7.x86_64会与fontconfig-2.10.95-11.el7.x86_64配合，而这个版本的fontconfig会安装一个lyx-fonts，而安装了lyx-fonts后，patcha无法正确的渲染字体，而是变成了类似象形图画，比如Windows下的某些Webding或symbol的字体显示。
 * 强行给RandomFontFactory指定字体也不行，解决方法是强行将lyx-fonts移除并重启tomcat。
 * 
 * @author NetSnake
 *
 */
@Service
public class PatchcaOrgCaptchaGenerator extends BaseService implements CaptchaGenerator {

	private final String defaultCharacters = "ABEFHKPRSWXY34689";
	private final String numberCharacters = "1234567890";
	private final int defaultMaxLength = 8;
	private final int defaultMinLength = 6;
	private final int defaultImageWidth = 180;
	private final int defaultImageHeight = 80;
	private final int defaultForeColor = new Color(0,0,255).getRGB();

	private final String DEFAULT_IMG_TYPE = "png";

	@Override
	public Captcha generate(CriteriaMap captchaCriteria) {
		if(captchaCriteria == null){
			captchaCriteria = CriteriaMap.create();
		}
		correctCriteriaValue(captchaCriteria);
		BufferedImage bufImage = new BufferedImage(captchaCriteria.getIntValue("imageWidth"), captchaCriteria.getIntValue("imageHeight"), BufferedImage.TYPE_INT_ARGB);
		/*Graphics2D g = bufImage.createGraphics();
		bufImage = g.getDeviceConfiguration().createCompatibleImage(bufImage.getWidth(), bufImage.getHeight(), Transparency.TRANSLUCENT);
		g.dispose();*/

		int backColor = captchaCriteria.getIntValue("backColor");
		if(backColor != 0){
			//设置背景色，否则默认是透明背景
			new SingleColorBackgroundFactory(new Color(backColor)).fillBackground(bufImage);
		}
		String identify = captchaCriteria.getStringValue("identify");
		if(identify != null && identify.equalsIgnoreCase("pgw")){
			MyCustomBackgroundFactory backgroundFactory = new MyCustomBackgroundFactory();  
			backgroundFactory.fillBackground(bufImage);
		}
		String foreColorConfig = captchaCriteria.getStringValue("foreColor");
		Color color = ColorUtil.parseColor(foreColorConfig);
		if(color == null) {
			color = new Color(0,0,0);
		}
		int minLength = captchaCriteria.getIntValue("minLength");
		int maxLength = captchaCriteria.getIntValue("maxLength");
		logger.debug("产生验证码，颜色:{},长度:{}=>{}", foreColorConfig, minLength, maxLength );
		ColorFactory colorFactory = new SingleColorFactory(color);
		FilterFactory filterFactory = new CurvesRippleFilterFactory(colorFactory);
		RandomFontFactory fontFactory = new RandomFontFactory();
		/*List<String> families = new ArrayList<String>();
		families.add("serif");
		fontFactory.setFamilies(families);*/
		TextRenderer textRenderer = new BestFitTextRenderer();
		textRenderer.setLeftMargin(10);
		textRenderer.setRightMargin(10);
		AdaptiveRandomWordFactory wordFactory = new AdaptiveRandomWordFactory();

		String word = captchaCriteria.getStringValue("word");

		String wordType = captchaCriteria.getStringValue("wordType");

		if(StringUtils.isBlank(word)){
			if(wordType == null){
				wordType = "";
			}
			if(wordType.equalsIgnoreCase("NUMBER")){
				wordFactory.setCharacters(numberCharacters);
			} else {
				wordFactory.setCharacters(defaultCharacters);
			}
			wordFactory.setMinLength(minLength);
			wordFactory.setMaxLength(maxLength);
			word = wordFactory.getNextWord();
		}

		textRenderer.draw(word, bufImage, fontFactory, colorFactory);
		if (captchaCriteria.getIntValue("mode") == 1){
			bufImage = filterFactory.applyFilters(bufImage); 
		}
		Captcha captcha = new Captcha();
		captcha.setWord(word);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufImage, DEFAULT_IMG_TYPE, bos);
			captcha.setImage(bos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("生成的验证码:" + word + "图片大小为" + captcha.getImage().length);


		return captcha;


	}



	private void correctCriteriaValue(CriteriaMap captchaCriteria) {


		if(captchaCriteria.getIntValue("foreColor") == 0){
			captchaCriteria.put("foreColor",defaultForeColor);
		}
		if(StringUtils.isBlank(captchaCriteria.getStringValue("characters"))){
			captchaCriteria.put("characters",defaultCharacters);
		}
		if(StringUtils.isBlank(captchaCriteria.getStringValue("imageFormat"))){
			captchaCriteria.put("imageFormat",DEFAULT_IMG_TYPE);
		}
		if(captchaCriteria.getIntValue("maxLength") == 0){
			captchaCriteria.put("maxLength",defaultMaxLength);
		}
		if(captchaCriteria.getIntValue("minLength") == 0){
			captchaCriteria.put("minLength",defaultMinLength);
		}
		if(captchaCriteria.getIntValue("imageHeight") == 0){
			captchaCriteria.put("imageHeight",defaultImageHeight);
		}
		if(captchaCriteria.getIntValue("imageWidth") == 0){
			captchaCriteria.put("imageWidth",defaultImageWidth);
		}
		if (StringUtils.isBlank(captchaCriteria.getStringValue("mode"))){
			captchaCriteria.put("mode",1);
			return;
		}

	}

	final class MyCustomBackgroundFactory implements BackgroundFactory {  
		private Random random = new Random();  

		public void fillBackground(BufferedImage image) {  
			Graphics graphics = image.getGraphics();  

			// 验证码图片的宽高  
			int imgWidth = image.getWidth();  
			int imgHeight = image.getHeight();  

			// 填充为白色背景  
			graphics.setColor(Color.WHITE);  
			graphics.fillRect(0, 0, imgWidth, imgHeight);  

			// 画100个噪点(颜色及位置随机)  
			for(int i = 0; i < 100; i++) {  
				// 随机颜色  
				int rInt = random.nextInt(255);  
				int gInt = random.nextInt(255);  
				int bInt = random.nextInt(255);  

				graphics.setColor(new Color(rInt, gInt, bInt));  

				// 随机位置  
				int xInt = random.nextInt(imgWidth - 3);  
				int yInt = random.nextInt(imgHeight - 2);  

				// 随机旋转角度  
				int sAngleInt = random.nextInt(360);  
				int eAngleInt = random.nextInt(360);  

				// 随机大小  
				int wInt = random.nextInt(6);  
				int hInt = random.nextInt(6);  

				graphics.fillArc(xInt, yInt, wInt, hInt, sAngleInt, eAngleInt);  

				// 画5条干扰线  
				if (i % 20 == 0) {  
					int xInt2 = random.nextInt(imgWidth);  
					int yInt2 = random.nextInt(imgHeight);  
					graphics.drawLine(xInt, yInt, xInt2, yInt2);  
				}  
			}  
		}  
	}  
}