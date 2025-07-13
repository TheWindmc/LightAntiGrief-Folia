package me.statuxia.lightantigrief.trigger.actions;

import lombok.Getter;
import me.statuxia.lightantigrief.config.LAGConfig;
import net.kyori.adventure.text.Component;

@Getter
public enum GriefAction {

    FIRE_CHARGE(Component.text(" sets the fire on ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getFireCharge();
        }
    },
    GET_ITEM(Component.text(" took ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getGetItem();
        }
    },
    PUT_ITEM(Component.text(" put ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getPutItem();
        }
    },
    BREAK_BLOCK(Component.text(" broke ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getBreakBlock();
        }
    },
    PLACE_BLOCK(Component.text(" placed ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getPlaceBlock();
        }
    },
    MINECART(Component.text(" placed ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getMinecart();
        }
    },
    EXPLODE(Component.text(" exploded ")) {
        @Override
        public int getLimitTriggers() {
            return LAGConfig.getExplode();
        }
    };

    private final Component message;

    GriefAction(Component message) {
        this.message = message;
    }

    /**
     * Получить лимит триггеров для данного действия
     * @return количество триггеров до срабатывания защиты
     */
    public abstract int getLimitTriggers();

    /**
     * Получить читаемое название действия
     * @return название действия
     */
    public String getActionName() {
        return name().toLowerCase().replace("_", " ");
    }

    /**
     * Проверить, является ли действие взрывом
     * @return true если действие связано со взрывом
     */
    public boolean isExplosive() {
        return this == EXPLODE || this == PLACE_BLOCK;
    }

    /**
     * Проверить, является ли действие работой с инвентарем
     * @return true если действие связано с инвентарем
     */
    public boolean isInventoryAction() {
        return this == GET_ITEM || this == PUT_ITEM;
    }
}