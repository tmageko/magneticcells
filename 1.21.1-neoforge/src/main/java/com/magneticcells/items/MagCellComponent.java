package com.magneticcells.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Кастомный DataComponent для хранения всех данных ячейки.
 * Хранится под ключом magneticcells:cell_data — GTCEu не затрагивает его.
 */
public record MagCellComponent(
    List<StoredEntry> storage,
    List<String> filterItems,
    int filterMode,
    boolean magnetEnabled,
    int energy
) {
    public record StoredEntry(String itemId, long amount) {
        public static final Codec<StoredEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(StoredEntry::itemId),
            Codec.LONG.fieldOf("n").forGetter(StoredEntry::amount)
        ).apply(i, StoredEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, StoredEntry> STREAM_CODEC =
            StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, StoredEntry::itemId,
                ByteBufCodecs.VAR_LONG,    StoredEntry::amount,
                StoredEntry::new
            );
    }

    public static final MagCellComponent EMPTY = new MagCellComponent(
        List.of(), List.of(), 0, true, 0
    );

    public static final Codec<MagCellComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
        StoredEntry.CODEC.listOf().optionalFieldOf("storage", List.of()).forGetter(MagCellComponent::storage),
        Codec.STRING.listOf().optionalFieldOf("filter", List.of()).forGetter(MagCellComponent::filterItems),
        Codec.INT.optionalFieldOf("fmode", 0).forGetter(MagCellComponent::filterMode),
        Codec.BOOL.optionalFieldOf("magnet", true).forGetter(MagCellComponent::magnetEnabled),
        Codec.INT.optionalFieldOf("energy", 0).forGetter(MagCellComponent::energy)
    ).apply(i, MagCellComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MagCellComponent> STREAM_CODEC =
        StreamCodec.composite(
            StoredEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), MagCellComponent::storage,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MagCellComponent::filterItems,
            ByteBufCodecs.INT,  MagCellComponent::filterMode,
            ByteBufCodecs.BOOL, MagCellComponent::magnetEnabled,
            ByteBufCodecs.INT,  MagCellComponent::energy,
            MagCellComponent::new
        );

    // ── Билдер для удобного изменения ──

    public MagCellComponent withStorage(List<StoredEntry> s)  { return new MagCellComponent(s, filterItems, filterMode, magnetEnabled, energy); }
    public MagCellComponent withFilter(List<String> f)        { return new MagCellComponent(storage, f, filterMode, magnetEnabled, energy); }
    public MagCellComponent withFilterMode(int m)             { return new MagCellComponent(storage, filterItems, m, magnetEnabled, energy); }
    public MagCellComponent withMagnet(boolean m)             { return new MagCellComponent(storage, filterItems, filterMode, m, energy); }
    public MagCellComponent withEnergy(int e)                 { return new MagCellComponent(storage, filterItems, filterMode, magnetEnabled, e); }

    // ── Конвертация StoredEntry ↔ CellData.StoredStack ──

    public List<CellData.StoredStack> toStoredStacks() {
        List<CellData.StoredStack> result = new ArrayList<>();
        for (StoredEntry e : storage) {
            ResourceLocation rl = ResourceLocation.tryParse(e.itemId());
            if (rl == null) continue;
            var item = BuiltInRegistries.ITEM.get(rl);
            if (item == null) continue;
            result.add(new CellData.StoredStack(new ItemStack(item), e.amount()));
        }
        return result;
    }

    public static List<StoredEntry> fromStoredStacks(List<CellData.StoredStack> stacks) {
        List<StoredEntry> result = new ArrayList<>();
        for (CellData.StoredStack ss : stacks) {
            if (ss.amount <= 0 || ss.type.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(ss.type.getItem());
            result.add(new StoredEntry(id.toString(), ss.amount));
        }
        return result;
    }

    public List<ItemStack> toFilterStacks() {
        List<ItemStack> result = new ArrayList<>();
        for (String id : filterItems) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) { result.add(ItemStack.EMPTY); continue; }
            var item = BuiltInRegistries.ITEM.get(rl);
            result.add(item == null ? ItemStack.EMPTY : new ItemStack(item));
        }
        return result;
    }

    public static List<String> filterFromStacks(List<ItemStack> stacks) {
        List<String> result = new ArrayList<>();
        for (ItemStack s : stacks) {
            if (s.isEmpty()) result.add("");
            else result.add(BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
        }
        return result;
    }
}
