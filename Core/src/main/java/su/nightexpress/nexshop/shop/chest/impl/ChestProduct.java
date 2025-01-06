package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private long quantity;

    public ChestProduct(@NotNull ShopPlugin plugin,
                        @NotNull String id,
                        @NotNull ChestShop shop,
                        @NotNull Currency currency,
                        @NotNull ProductTyping typing) {
        super(plugin, id, shop, currency, typing);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.writeQuantity(config, path);

        config.set(path + ".Type", this.type.type().name());
        this.type.write(config, path);

        config.set(path + ".Currency", this.getCurrency().getInternalId());
        this.getPricer().write(config, path + ".Price");
    }

    public void writeQuantity(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".InfiniteStorage.Quantity", this.quantity);
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forChestProduct(this, player);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(this.plugin, player, this, buyType, all);
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        return this.shop.getStock().count(this, tradeType, player);
    }

    @Override
    public boolean isAvailable(@NotNull Player player) {
        return this.shop.isActive();
    }

    /**
     *
     * @return Product quantity for Infinite Storage system.
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Sets product's quantity for Infinite Storage system.
     * @param quantity Product quantity.
     */
    public void setQuantity(long quantity) {
        this.quantity = Math.max(0, Math.abs(quantity));
    }
}
