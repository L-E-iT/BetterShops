package max.hubbard.bettershops.Shops.Types.Holo.Icons;

import max.hubbard.bettershops.Configurations.Language;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import max.hubbard.bettershops.Configurations.Permissions;
import max.hubbard.bettershops.Core;
import max.hubbard.bettershops.Menus.MenuType;
import max.hubbard.bettershops.Shops.Items.ShopItem;
import max.hubbard.bettershops.Shops.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * ***********************************************************************
 * Copyright Max Hubbard (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated documents with similar branding
 * are the sole property of Max. Distribution, reproduction, taking snippets, or
 * claiming any contents as your own will break the terms of the license, and void any
 * agreements with you, the third party.
 * ************************************************************************
 */
public class ShopIcon {

    ShopItem item;
    Shop shop;
    NamedHologram h;

    public ShopIcon(final ShopItem item){
        this.item = item;
        if (item == null) return;
        shop = item.getShop();

        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
            @Override
            public void run() {
                h = new NamedHologram(shop.getLocation().clone().add(.5, 1.75, .5), "BSIcon" + shop.getName().replaceAll(" ", "_"));

                h.appendItemLine(item.getItem()).setTouchHandler(new TouchHandler() {
                    @Override
                    public void onTouch(Player player) {
                        boolean isBlacklisted = shop.getBlacklist().contains(player);
                        boolean canBypass = Permissions.hasBypassBlacklistPerm(player);

                        if (item.isSelling()) {
                            if (player.getUniqueId().toString().equals(shop.getOwner().getUniqueId().toString()) && !shop.isServerShop()) {
                                shop.getMenu(MenuType.ITEM_MANAGER_SELLING).draw(player,item.getPage(),item);
                            } else {
                                if (!isBlacklisted || canBypass) {
                                    if (!Core.getEconomy().hasAccount(Bukkit.getOfflinePlayer(player.getUniqueId())) || !Core.getEconomy().hasAccount(Bukkit.getOfflinePlayer(shop.getOwner().getUniqueId()))) {
                                        player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("BuyingAndSelling", "NoAccountLore"));
                                    } else {
                                        if (isBlacklisted && canBypass){
                                            player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "BypassingBlacklist"));
                                        }
                                        shop.getMenu(MenuType.SELL_ITEM).draw(player, item.getPage(), item.getItem(), item);
                                    }
                                } else {
                                    player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "NotAllowed"));
                                }
                            }


                        } else {

                            if (player.getUniqueId().toString().equals(shop.getOwner().getUniqueId().toString()) && !shop.isServerShop()) {
                                shop.getMenu(MenuType.ITEM_MANAGER_BUYING).draw(player,item.getPage(),item);
                            } else {
                                if (!shop.getBlacklist().contains(player) || Permissions.hasBypassBlacklistPerm(player)) {
                                    if (!Core.getEconomy().hasAccount(Bukkit.getOfflinePlayer(player.getUniqueId())) || !Core.getEconomy().hasAccount(Bukkit.getOfflinePlayer(shop.getOwner().getUniqueId()))) {
                                        player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("BuyingAndSelling", "NoAccountLore"));
                                    } else {
                                        if (isBlacklisted && canBypass){
                                            player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "BypassingBlacklist"));
                                        }
                                        shop.getMenu(MenuType.BUY_ITEM).draw(player, item.getPage(), item.getItem(), item);
                                    }
                                } else {
                                    player.sendMessage(Language.getString("Messages", "Prefix") + Language.getString("Messages", "NotAllowed"));
                                }
                            }
                        }
                    }
                });
            }
        });



    }

    public NamedHologram getHologram(){
        return h;
    }

    public ShopItem getItem(){
        return item;
    }
}
