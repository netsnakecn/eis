package com.maicard.mb.constants;

public enum SyncMode {
    SYNC, ASYNC;

    public static SyncMode find(String v) {
        if(v == null){
            return ASYNC;
        }
        for( SyncMode syncMode : SyncMode.values()){
            if(v.equalsIgnoreCase(syncMode.name())){
                return syncMode;
            }
        }
        return ASYNC;
    }
}
