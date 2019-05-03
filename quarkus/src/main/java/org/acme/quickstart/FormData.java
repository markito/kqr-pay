package org.acme.quickstart;

import java.io.File;

import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class FormData {

    private File qrFile;

    public File getQrFile() {
        return qrFile;
    }

    @FormParam("qrFile")
    @PartType("application/octet-stream")
    public void setQrFile(File qrFile) {
        this.qrFile = qrFile;
    }
}