package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class StaticProduct extends AbstractVirtualProduct<StaticShop> {

    private int shopSlot;
    private int shopPage;

    public StaticProduct(@NotNull ShopPlugin plugin,
                         @NotNull String id,
                         @NotNull StaticShop shop,
                         @NotNull Currency currency,
                         @NotNull ProductTyping type) {
        super(plugin, id, shop, currency, type);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config, @NotNull String path) {
        this.setSlot(config.getInt(path + ".Shop_View.Slot", -1));
        this.setPage(config.getInt(path + ".Shop_View.Page", -1));
        this.setDiscountAllowed(config.getBoolean(path + ".Discount.Allowed"));
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Discount.Allowed", this.discountAllowed);
        config.set(path + ".Shop_View.Slot", this.shopSlot);
        config.set(path + ".Shop_View.Page", this.shopPage);
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forStaticProduct(this, player);
    }

    public int getSlot() {
        return this.shopSlot;
    }

    public void setSlot(int slot) {
        this.shopSlot = slot;
    }

    public int getPage() {
        return this.shopPage;
    }

    public void setPage(int page) {
        this.shopPage = page;
    }
}
