package com.pyramidplundercounter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PyramidPlunderCounterData {

    PyramidPlunderCounterData(int chestsLooted, int sarcoLooted, int chestSinceLastSceptre, int sarcoSinceLastSceptre, Double chanceOfBeingDry, Double petChanceOfBeingDry) {
        this.chestsLooted = chestsLooted;
        this.sarcoLooted = sarcoLooted;
        this.chestSinceLastSceptre = chestSinceLastSceptre;
        this.sarcoSinceLastSceptre = sarcoSinceLastSceptre;
        this.chanceOfBeingDry = chanceOfBeingDry;
        this.petChanceOfBeingDry = petChanceOfBeingDry;
    }

    PyramidPlunderCounterData() {
        this.chestsLooted = 0;
        this.sarcoLooted = 0;
        this.chestSinceLastSceptre = 0;
        this.sarcoSinceLastSceptre = 0;
        this.chanceOfBeingDry = 1.0;
        this.petChanceOfBeingDry = 1.0;
    }

    @Expose
    @SerializedName("successful-chests")
    private final int chestsLooted;

    @Expose
    @SerializedName("successful-sarcophagi")
    private final int sarcoLooted;

    @Expose
    @SerializedName("chest-since-last-sceptre")
    private final int chestSinceLastSceptre;

    @Expose
    @SerializedName("sarco-since-last-sceptre")
    private final int sarcoSinceLastSceptre;

    @Expose
    @SerializedName("chance-of-being-dry")
    private final Double chanceOfBeingDry;

    @Expose
    @SerializedName("pet-chance-of-being-dry")
    private final Double petChanceOfBeingDry;

    public int getChestsLooted() {
        return chestsLooted;
    }

    public int getSarcoLooted() {
        return sarcoLooted;
    }

    public int getChestSinceLastSceptre() {
        return chestSinceLastSceptre;
    }

    public int getSarcoSinceLastSceptre() {
        return sarcoSinceLastSceptre;
    }

    public Double getChanceOfBeingDry() {
        return chanceOfBeingDry;
    }

    public Double getPetChanceOfBeingDry() {
        return petChanceOfBeingDry;
    }
}
