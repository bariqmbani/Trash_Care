package com.unpad.trashcare;

import android.app.Application;


public class UserClient extends Application {

    private Warga warga;

    public Warga getWarga() {
        return warga;
    }

    public void setWarga(Warga warga) {
        this.warga = warga;
    }
}
