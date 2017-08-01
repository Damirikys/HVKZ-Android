package org.hvkz.hvkz.uapi.extensions;

@SuppressWarnings("unused")
public class Parameters
{
    private String chest;
    private String underchest;
    private String weight;
    private String growth;
    private String desiredWeight;
    private String waistCirc;
    private String girthPelvis;
    private String girthButtocks;
    private String hipCirc;

    public String getChest() {
        return chest.trim();
    }

    public String getUnderchest() {
        return underchest.trim();
    }

    public String getWeight() {
        return weight.trim();
    }

    public String getGrowth() {
        return growth.trim();
    }

    public String getDesiredWeight() {
        return desiredWeight.trim();
    }

    public String getWaistCirc() {
        return waistCirc.trim();
    }

    public String getGirthPelvis() {
        return girthPelvis.trim();
    }

    public String getGirthButtocks() {
        return girthButtocks.trim();
    }

    public String getHipCirc() {
        return hipCirc.trim();
    }
}