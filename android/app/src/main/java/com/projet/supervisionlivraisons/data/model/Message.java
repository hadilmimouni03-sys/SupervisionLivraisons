package com.projet.supervisionlivraisons.data.model;

import com.google.gson.annotations.SerializedName;

/** Message row exchanged via {@code /api/messages/*}. */
public class Message {
    public int    id;
    public String type;     // "INFO" | "URGENCE"
    public String content;
    public Integer nocde;

    @SerializedName("sender_id")    public int    senderId;
    @SerializedName("receiver_id")  public Integer receiverId;
    @SerializedName("sender_nom")   public String senderNom;
    @SerializedName("created_at")   public String createdAt;
}
