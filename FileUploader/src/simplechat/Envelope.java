/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.Serializable;

/**
 *
 * @author cheema6606
 */
public class Envelope implements Serializable {
        
    public Envelope() {}
    
    public Envelope(String FileName, byte[] FileData) {
        fileName = FileName;
        fileData = FileData;   
    }
    
    private String fileName;
    private byte fileData[] = new byte[]{};
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileData
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * @param fileData the fileData to set
     */
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}
