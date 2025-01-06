package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.product.typing.CommandTyping;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class ProductMainEditor extends EditorMenu<ShopPlugin, VirtualProduct> implements ShopEditor {

    private static final String TEXTURE_COMMAND = "5f4c21d17ad636387ea3c736bff6ade897317e1374cd5d9b1c15e6e8953432";
    private static final String TEXTURE_META    = "1789b3e2868d716a921dec5932d530a892f600235f187766bc02d145ed16865b";

    private final VirtualShopModule module;

    public ProductMainEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Product Editor"), MenuSize.CHEST_54);
        this.module = module;

        this.addReturn(49, (viewer, event, product) -> {
            int page = product instanceof StaticProduct staticProduct ? staticProduct.getPage() : 1;
            this.runNextTick(() -> this.module.openProductsEditor(viewer.getPlayer(), product.getShop(), page));
        });

        // =============================================
        // Rotating Product stuff
        // =============================================
        this.addItem(Material.ENDER_EYE, VirtualLocales.PRODUCT_ROTATION_CHANCE, 13, (viewer, event, product) -> {
            RotatingProduct rotatingProduct = (RotatingProduct) product;
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_VALUE, (dialog, input) -> {
                rotatingProduct.setRotationChance(input.asDouble());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().addVisibilityPolicy(viewer -> this.getLink(viewer) instanceof RotatingProduct);

        // =============================================
        // Command Product stuff
        // =============================================
        this.addItem(ItemUtil.getSkinHead(TEXTURE_COMMAND), VirtualLocales.PRODUCT_COMMANDS, 10, (viewer, event, product) -> {
            CommandTyping type = (CommandTyping) product.getType();

            if (event.isRightClick()) {
                type.getCommands().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_COMMAND, (dialog, input) -> {
                type.getCommands().add(input.getText());
                this.saveProduct(viewer, product);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof CommandTyping);

        // =============================================
        // Item Product stuff
        // =============================================
        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_ITEM, 10, (viewer, event, product) -> {
            if (event.isRightClick()) {
                PhysicalTyping packer = (PhysicalTyping) product.getType();
                Players.addItem(viewer.getPlayer(), packer.getItem());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            boolean isBypass = event.isShiftClick();
            ProductTyping typing = ProductTypes.fromItem(cursor, isBypass);

            product.setType(typing);
            event.getView().setCursor(null);
            this.saveProductAndFlush(viewer, product);
        }).getOptions()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof PhysicalTyping)
            .setDisplayModifier((viewer, item) -> {
                PhysicalTyping itemPacker = (PhysicalTyping) this.getLink(viewer).getType();
                ItemStack original = itemPacker.getItem();
                item.setType(original.getType());
                item.setAmount(original.getAmount());
                item.setItemMeta(original.getItemMeta());
                ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_ITEM).hideFlags().writeMeta();
            });

        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_PREVIEW, 13, (viewer, event, product) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), product.getPreview());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            CommandTyping type = (CommandTyping) product.getType();
            type.setPreview(cursor);
            event.getView().setCursor(null);
            this.saveProductAndFlush(viewer, product);
        }).getOptions()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof CommandTyping)
            .setDisplayModifier((viewer, item) -> {
                ItemStack original = this.getLink(viewer).getType().getPreview();
                item.setType(original.getType());
                item.setAmount(original.getAmount());
                item.setItemMeta(original.getItemMeta());
                ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_PREVIEW).hideFlags().writeMeta();
            });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_META), VirtualLocales.PRODUCT_RESPECT_ITEM_META, 13, (viewer, event, product) -> {
            if (!(product.getType() instanceof VanillaTyping typing)) return;

            typing.setRespectMeta(!typing.isRespectMeta());
            this.saveProductAndFlush(viewer, product);
        }).getOptions().setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof VanillaTyping);

        // =============================================
        // Regular Product stuff
        // =============================================
        this.addItem(Material.EMERALD, VirtualLocales.PRODUCT_PRICE_MANAGER, 16, (viewer, event, product) -> {
            if (event.getClick() == ClickType.DROP) {
                product.getShop().getPricer().updatePrice(product);
                this.saveProductAndFlush(viewer, product);
            }
            else if (event.isLeftClick()) {
                this.runNextTick(() -> this.module.openPriceEditor(viewer.getPlayer(), product));
            }
        });

        this.addItem(Material.GOLDEN_HELMET, VirtualLocales.PRODUCT_RANKS_REQUIRED, 29, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getAllowedRanks().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_RANK, (dialog, input) -> {
                product.getAllowedRanks().add(input.getTextRaw().toLowerCase());
                this.saveProduct(viewer, product);
                return true;
            });
        });

        this.addItem(Material.REDSTONE, VirtualLocales.PRODUCT_PERMISIONS_REQUIRED, 33, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getRequiredPermissions().clear();
                this.saveProductAndFlush(viewer, product);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_PERMISSION, (dialog, input) -> {
                product.getRequiredPermissions().add(input.getTextRaw());
                this.saveProduct(viewer, product);
                return true;
            });
        });

        this.addItem(Material.BARREL, VirtualLocales.PRODUCT_STOCK, 31, (viewer, event, product) -> {
            this.runNextTick(() -> this.module.openStockEditor(viewer.getPlayer(), product));
        });

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).replacePlaceholders());
        }));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.setTitle(this.getLink(viewer).replacePlaceholders().apply(options.getTitle()));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }
}
