package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Players;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractVirtualProduct<S extends AbstractVirtualShop<?>> extends AbstractProduct<S> implements VirtualProduct {

    protected StockValues stockValues;
    protected StockValues limitValues;
    protected Set<String> allowedRanks;
    protected Set<String> requiredPermissions;
    protected boolean     discountAllowed;

    public AbstractVirtualProduct(@NotNull ShopPlugin plugin,
                                  @NotNull String id,
                                  @NotNull S shop,
                                  @NotNull Currency currency,
                                  @NotNull ProductTyping type) {
        super(plugin, id, shop, currency, type);
        this.allowedRanks = new HashSet<>();
        this.requiredPermissions = new HashSet<>();
        this.stockValues = new StockValues();
        this.limitValues = new StockValues();
    }

    public void load(@NotNull FileConfig config, @NotNull String path) {
        this.setAllowedRanks(config.getStringSet(path + ".Allowed_Ranks"));
        this.setRequiredPermissions(config.getStringSet(path + ".Required_Permissions"));
        this.setPricer(AbstractProductPricer.read(config, path + ".Price"));
        this.setStockValues(StockValues.read(config, path + ".Stock.GLOBAL"));
        this.setLimitValues(StockValues.read(config, path + ".Stock.PLAYER"));
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Type", this.type.type().name());
        this.type.write(config, path);

        config.set(path + ".Allowed_Ranks", this.getAllowedRanks());
        config.set(path + ".Required_Permissions", this.getRequiredPermissions());
        if (!this.getCurrency().isDummy()) {
            config.set(path + ".Currency", this.getCurrency().getInternalId());
        }
        this.getPricer().write(config, path + ".Price");
        this.getStockValues().write(config, path + ".Stock.GLOBAL");
        this.getLimitValues().write(config, path + ".Stock.PLAYER");
        this.writeAdditional(config, path);
    }

    protected abstract void loadAdditional(@NotNull FileConfig config, @NotNull String path);

    protected abstract void writeAdditional(@NotNull FileConfig config, @NotNull String path);

    @Override
    @NotNull
    public VirtualPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new VirtualPreparedProduct(this.plugin, player, this, buyType, all);
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        if (!this.getRequiredPermissions().isEmpty()) {
            if (this.getRequiredPermissions().stream().noneMatch(player::hasPermission)) {
                return false;
            }
        }

        if (!this.getAllowedRanks().isEmpty()) {
            Set<String> ranks = Players.getPermissionGroups(player);
            return ranks.stream().anyMatch(rank -> this.getAllowedRanks().contains(rank));
        }

        return true;
    }

    @Override
    public boolean isAvailable(@NotNull Player player) {
        if (!this.hasAccess(player)) {
            Lang.ERROR_NO_PERMISSION.getMessage(this.plugin).send(player);
            return false;
        }

        return true;
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        int inStock = this.shop.getStock().count(this, tradeType);
        int inLimit = this.shop.getStock().count(this, tradeType, player);

        // If both, stock & limit, are present, return minimal of them.
        if (inStock >= 0 && inLimit >= 0) return Math.min(inStock, inLimit);

        // In other case return the one that is not unlimited.
        return inStock < 0 ? inLimit : inStock;
    }

    @NotNull
    public StockValues getStockValues() {
        return stockValues;
    }

    public void setStockValues(@NotNull StockValues stockValues) {
        this.stockValues = stockValues;
    }

    @NotNull
    public StockValues getLimitValues() {
        return limitValues;
    }

    public void setLimitValues(@NotNull StockValues limitValues) {
        this.limitValues = limitValues;
    }

    @Override
    @NotNull
    public Set<String> getAllowedRanks() {
        return allowedRanks;
    }

    @Override
    public void setAllowedRanks(@NotNull Set<String> allowedRanks) {
        this.allowedRanks = allowedRanks;
    }

    @Override
    @NotNull
    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    @Override
    public void setRequiredPermissions(@NotNull Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    @Override
    public boolean isDiscountAllowed() {
        return discountAllowed;
    }

    @Override
    public void setDiscountAllowed(boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
    }
}
