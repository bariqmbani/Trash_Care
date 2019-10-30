package com.unpad.trashcare;

import android.app.Application;

import com.unpad.trashcare.models.Warga;


public class UserClient extends Application {

    private Warga warga;

    public Warga getWarga() {
        return warga;
    }

    public void setWarga(Warga warga) {
        this.warga = warga;
    }
}
