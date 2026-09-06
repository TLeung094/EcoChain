package me.tleung.ecochain.data;

import org.bukkit.inventory.ItemStack;

public class EcoQuest {
    public enum QuestType {
        PLANT_TREE, BREED_ANIMAL, CLEAN_LAVA
    }

    private final String id;
    private final String description;
    private final QuestType type;
    private final int targetAmount;
    private final int expReward;
    private final ItemStack itemReward;

    public EcoQuest(String id, String description, QuestType type, int targetAmount, int expReward, ItemStack itemReward) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetAmount = targetAmount;
        this.expReward = expReward;
        this.itemReward = itemReward;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public int getTargetAmount() { return targetAmount; }
    public int getExpReward() { return expReward; }
    public ItemStack getItemReward() { return itemReward; }
}