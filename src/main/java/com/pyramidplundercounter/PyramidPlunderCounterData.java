package com.pyramidplundercounter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PyramidPlunderCounterData {

    PyramidPlunderCounterData(int totalChests, int totalsarcophagi, int successfulChests, int successfulsarcophagi, Double chanceOfBeingDry) {
        this.totalChests = totalChests;
        this.totalsarcophagi = totalsarcophagi;
        this.successfulChests = successfulChests;
        this.successfulsarcophagi = successfulsarcophagi;
        this.chanceOfBeingDry = chanceOfBeingDry;
    }

    PyramidPlunderCounterData() {
        this.totalChests = 0;
        this.totalsarcophagi = 0;
        this.successfulChests = 0;
        this.successfulsarcophagi = 0;
        this.chanceOfBeingDry = 1.0;
    }

    @Expose
    @SerializedName("total-chests")
    private final int totalChests;

    @Expose
    @SerializedName("total-sarcophagi")
    private final int totalsarcophagi;

    @Expose
    @SerializedName("successful-chests")
    private final int successfulChests;

    @Expose
    @SerializedName("successful-sarcophagi")
    private final int successfulsarcophagi;

    @Expose
    @SerializedName("chance-of-being-dry")
    private final Double chanceOfBeingDry;

    public int getTotalChests() {
        return totalChests;
    }

    public int getTotalsarcophagi() {
        return totalsarcophagi;
    }

    public int getSuccessfulChests() {
        return successfulChests;
    }

    public int getSuccessfulsarcophagi() {
        return successfulsarcophagi;
    }

    public Double getChanceOfBeingDry() {
        return chanceOfBeingDry;
    }
}
