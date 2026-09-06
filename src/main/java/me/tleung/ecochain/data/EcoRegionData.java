package me.tleung.ecochain.data;

import java.util.concurrent.atomic.AtomicInteger;

public class EcoRegionData {
    private final String chunkKey;
    private final AtomicInteger flora;
    private final AtomicInteger fauna;
    private final AtomicInteger aqua;

    public EcoRegionData(String chunkKey, int flora, int fauna, int aqua) {
        this.chunkKey = chunkKey;
        this.flora = new AtomicInteger(flora);
        this.fauna = new AtomicInteger(fauna);
        this.aqua = new AtomicInteger(aqua);
    }

    public String getChunkKey() { return chunkKey; }
    public AtomicInteger getFlora() { return flora; }
    public AtomicInteger getFauna() { return fauna; }
    public AtomicInteger getAqua() { return aqua; }

    public int getOverallHealth() {
        return (flora.get() + fauna.get() + aqua.get()) / 3;
    }
}