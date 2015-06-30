package max.hubbard.bettershops;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import max.hubbard.bettershops.Commands.BSCommand;
import max.hubbard.bettershops.Configurations.Blacklist;
import max.hubbard.bettershops.Configurations.Config;
import max.hubbard.bettershops.Configurations.ConfigMenu.ConfigMenuListener;
import max.hubbard.bettershops.Configurations.ConfigMenu.NPCChooser;
import max.hubbard.bettershops.Configurations.Language;
import max.hubbard.bettershops.Configurations.LanguageMenu.GUIMessageListener;
import max.hubbard.bettershops.Configurations.LanguageMenu.LanguageInventory;
import max.hubbard.bettershops.Listeners.*;
import max.hubbard.bettershops.SQL.Database;
import max.hubbard.bettershops.SQL.mysql.MySQL;
import max.hubbard.bettershops.Shops.Shop;
import max.hubbard.bettershops.Shops.Types.Holo.HologramManager;
import max.hubbard.bettershops.Shops.Types.NPC.NPCManager;
import max.hubbard.bettershops.Shops.Types.NPC.ShopsNPC;
import max.hubbard.bettershops.Shops.Types.Sign.CreateSign;
import max.hubbard.bettershops.Shops.Types.Sign.Purchase;
import max.hubbard.bettershops.Shops.Types.Sign.SignShopManager;
import max.hubbard.bettershops.Utils.ChatManager;
import max.hubbard.bettershops.Utils.Conversion;
import max.hubbard.bettershops.Utils.Metrics;
import max.hubbard.bettershops.Versions.AnvilGUI;
import max.hubbard.bettershops.Versions.TitleManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;

/**
 * ***********************************************************************
 * Copyright Max Hubbard (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated documents with similar branding
 * are the sole property of Max. Distribution, reproduction, taking snippets, or
 * claiming any contents as your own will break the terms of the license, and void any
 * agreements with you, the third party.
 * ************************************************************************
 */
public class Core extends JavaPlugin {

    //TODO:


    private static Core instance;
    private static AnvilGUI gui;
    public static Metrics metrics;
    private static Economy economy;
    private static TitleManager manager;
    private static boolean aboveEight = false;
    private static boolean holo = false;
    private static boolean wg = false;
    private static boolean beta = false;
    private static Connection c;
    private static Database db;

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §eSaving Shops..");
        for (Shop shop : ShopManager.getShops()) {
            if (shop.isHoloShop()){
                shop.getHolographicShop().getHologram().delete();
            }
            shop.saveConfig();
        }
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §eSaved!");
    }

    @Override
    public void onEnable() {

        if (getVault() != null && setupEconomy()) {

            try {

                Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aEnabling BetterShops §e" + getDescription().getVersion());

                instance = this;

                //REGISTER LISTENERS
                Bukkit.getPluginManager().registerEvents(new ClickableActionListener(), this);
                Bukkit.getPluginManager().registerEvents(new AddItemListener(), this);
                Bukkit.getPluginManager().registerEvents(new ChatManager(), this);
                Bukkit.getPluginManager().registerEvents(new Blacklist(), this);
                Bukkit.getPluginManager().registerEvents(new CreateShop(), this);
                Bukkit.getPluginManager().registerEvents(new CreateSign(), this);
                Bukkit.getPluginManager().registerEvents(new NPCOpen(), this);
                Bukkit.getPluginManager().registerEvents(new Purchase(), this);
                Bukkit.getPluginManager().registerEvents(new Opener(), this);
                Bukkit.getPluginManager().registerEvents(new Delete(), this);
                Bukkit.getPluginManager().registerEvents(new ShopMaintainer(), this);
                Bukkit.getPluginManager().registerEvents(new ConfigMenuListener(), this);
                Bukkit.getPluginManager().registerEvents(new NPCChooser(), this);
                Bukkit.getPluginManager().registerEvents(new GUIMessageListener(), this);
                Bukkit.getPluginManager().registerEvents(new LanguageInventory(), this);

                //REGISTER COMMANDS
                getCommand("BS").setExecutor(new BSCommand());

                //UPDATE FILES
                Language.updateFiles();

                //REGISTER INTERFACES
                String packageName = this.getServer().getClass().getPackage().getName();

                String v = this.getServer().getVersion();

                // Get full package string of CraftServer.
                // org.bukkit.craftbukkit.version
                String version = packageName.substring(packageName.lastIndexOf('.') + 1);
                // Get the last element of the package

                if (Integer.parseInt(version.substring(3, version.length() - 3)) >= 8) {
                    aboveEight = true;
                }


                //Choose Anvil GUI for Version
                try {
                    final Class<?> clazz = Class.forName("max.hubbard.bettershops.Versions." + version + ".AnvilGUI");
                    // Check if we have a NMSHandler class at that location.
                    if (clazz != null) {
                        if (AnvilGUI.class.isAssignableFrom(clazz)) { // Make sure it actually implements NMS
                            gui = (AnvilGUI) clazz.getConstructor().newInstance(); // Set our handler
                        }
                    } else {
                        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §cCould not find support for this CraftBukkit version. Currently Supports CB version 1.7.9 RO.3 - Spigot 1.8.R1, You are using §d" + version + "§c. Plugin Disabling!");
                        this.setEnabled(false);
                        return;
                    }

                    boolean c = false;

                    if (v.contains("Spigot")) {
                        if (version.equals("v1_7_R4") || version.equals("v1_8_R1") || version.equals("v1_8_R2") || version.equals("v1_8_R3")) {
                            c = true;
                        }
                    }


                    if (c) {
                        final Class<?> clazz2 = Class.forName("max.hubbard.bettershops.Versions." + version + ".TitleManager");
                        // Check if we have a NMSHandler class at that location.

                        if (clazz2 != null) {
                            if (TitleManager.class.isAssignableFrom(clazz2)) { // Make sure it actually implements NMS
                                manager = (TitleManager) clazz2.getConstructor().newInstance(); // Set our handler
                            }
                        } else {
                            aboveEight = false;

                            return;
                        }
                    }
                } catch (final Exception e) {
//                e.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §cCould not find support for this CraftBukkit version. Currently Supports CB version 1.7.9 RO.3 - Spigot 1.8.R1, You are using §d" + version + "§c. Plugin Disabling!");
                    this.setEnabled(false);
                    return;
                }

                //REGISTER ADD-ONS

                Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading support for CraftBukkit §e" + version.replaceAll("_", "."));

                if (getWorldGuard() != null) {
                    wg = true;
                    Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading support for §eWorldGuard");
                }

                if (getHolographicDisplays() != null) {
                    holo = true;
                    Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading support for §eHolographic Displays");
                }

                if ((boolean) Config.getObject("Metrics")) {
                    metrics = new Metrics(this);
                    setUpMetrics();
                    metrics.start();
                    Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aUsing §eMetrics §7- http://mcstats.org");
                }
                if (Config.getObject("SQL").equals("true") || (boolean) Config.getObject("SQL")) {
                    String user = (String) Config.getObject("username");
                    String pass = (String) Config.getObject("password");
                    String host = (String) Config.getObject("host");
                    int port = (int) Config.getObject("port");
                    String dataBase = (String) Config.getObject("database");
                    db = new MySQL(this, host, String.valueOf(port), dataBase, user, pass);

                    c = db.openConnection();
                }

                //OTHER CHANGES
                Language.moveMessagesFile();

                //LOAD SHOPS
                if (Conversion.checkConversion()) {
                    Conversion.startConversion();
                }
                loadShops();
                Updater.startCheckForUpdate();

            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §cAn error occurred! §cPlease inform the developer @ §ehttp://dev.bukkit.org/bukkit-plugins/better-shops/ §c. Plugin Disabling!");
                e.printStackTrace();
                this.setEnabled(false);
            }
        } else {

            Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §cI do Not see §dVault §cIn your plugins folder. This means that §bBetter Shops §cis Not able to Function and will now §dDisable");
            this.setEnabled(false);

        }
    }

    public static Core getCore() {
        return instance;
    }

    public File getFile() {
        File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        return file;
    }

    public static boolean useHolograms() {
        return holo;
    }

    public static Metrics getMetrics() {
        return metrics;
    }

    public String getJarName() {
        return getFile().getName();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isAboveEight() {
        return aboveEight;
    }

    public static boolean useWorldGuard() {
        return wg;
    }

    //Get Anvil GUI
    public static AnvilGUI getAnvilGUI() {
        if (gui != null) {
            try {
                return gui.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                return gui;
            }
        }
        return null;
    }

    //Get Title Manager
    public static TitleManager getTitleManager() {
        if (manager != null) {
            try {
                return manager.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                return manager;
            }
        }
        return null;
    }

    //Get WG
    public static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin) || !plugin.isEnabled()) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    //Get Vault
    private Plugin getVault() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Vault");

        // Vault may not be loaded
        if (plugin == null || !plugin.isEnabled()) {
            return null; // Maybe you want throw an exception instead
        }

        return plugin;
    }

    //Get Holo
    private Plugin getHolographicDisplays() {
        Plugin plugin = getServer().getPluginManager().getPlugin("HolographicDisplays");

        // HD may not be loaded
        if (plugin == null || !plugin.isEnabled()) {
            return null; // Maybe you want throw an exception instead
        }

        return plugin;
    }


    public void setUpMetrics() {
        Metrics.Graph shops = metrics.createGraph("Number of Shops per Server");

        shops.addPlotter(new Metrics.Plotter("" + ShopManager.getShops().size()) {
            @Override
            public int getValue() {
                return 1;
            }
        });

        Metrics.Graph npcshops = metrics.createGraph("Number of NPC Shops per Server");

        npcshops.addPlotter(new Metrics.Plotter("" + NPCManager.getNPCShops().size()) {
            @Override
            public int getValue() {
                return 1;
            }
        });
//
        if (useHolograms()) {
            Metrics.Graph holoshops = metrics.createGraph("Number of Holographic Shops per Server");

            holoshops.addPlotter(new Metrics.Plotter("" + HologramManager.getHolographicShops().size()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }


        if (beta) {

            Metrics.Graph beta = metrics.createGraph("Beta users");

            beta.addPlotter(new Metrics.Plotter(getDescription().getVersion()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }

        if (useWorldGuard()) {
            Metrics.Graph wg = metrics.createGraph("WorldGuard users");

            wg.addPlotter(new Metrics.Plotter(Core.getWorldGuard().getDescription().getVersion()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });

        }
    }

    public static Connection getConnection() {
        return c;
    }

    public static Database getSQLDatabase() {
        return db;
    }

    public static boolean useSQL() {
        return db != null;
    }

    public static void loadShops() throws Exception {
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoaded §d" + ShopManager.loadShops() + " §aChest Shops");
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §eSome shops may still be initializing, this may last a few minutes");
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading §eSign Shops§a...");
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoaded §d" + SignShopManager.loadSignShops() + " §aSign Shops");

        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading §eNPC Shops§a...");
        //Load The Shops

//        for (Location l : ShopManager.locs.keySet()){
//            l.getBlock().get.
//        }

        for (World w : ShopManager.worlds) {
            if (w != null)
                for (final LivingEntity e : w.getLivingEntities()) {

                    if (!(e instanceof ArmorStand)) {

                        if (e.getCustomName() != null) {
                            if (e.getCustomName().contains("§a§l")) {
                                final Shop shop = ShopManager.fromString(e.getCustomName().substring(4));
                                if (shop != null) {
                                    if (!shop.isNPCShop() || shop.getNPCShop() == null) {
//                                        EntityCheck.isNPC(e);
//                                        if (EntityCheck.isNPC(e)) {
                                        ShopsNPC s = new ShopsNPC(e, shop);
                                        NPCManager.addNPCShop(s);
                                        s.removeChest();
                                        shop.setObject("NPC", true);
//                                        } else {
//                                            Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("BetterShops"), new Runnable() {
//                                                @Override
//                                                public void run() {
//
//                                                    ShopsNPC s = new ShopsNPC(e.getType(), Arrays.asList(""), shop, false, false, false);
//
//                                                    NPCManager.addNPCShop(s);
//                                                    s.removeChest();
//                                                    shop.setObject("NPC", true);
//                                                    e.remove();
//                                                }
//                                            });
//                                        }
                                    } else {
                                        e.remove();
                                    }
                                }
                            }
                        }
                    }
                }
        }

        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoaded §d" + NPCManager.getNPCShops().size() + " §aNPC Shops");

        if (useHolograms() && (boolean) Config.getObject("HoloShops")) {
            Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aHolographic Shops will be loaded when needed.");
        }
        Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aDone");
//            Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoading §eHologram Shops§a...");
//            Set<String> savedHologramsNames = HologramDatabase.getHolograms();
//            for (final String s : savedHologramsNames) {
//                System.out.println(s);
//
//                if (s.startsWith("BS")) {
//
//                    final Shop shop = ShopManager.fromString(s.substring(2).replaceAll("_", " "));
//
//                    if (shop != null) {
//                        System.out.println("Not Null | " + shop.getName());
//                        try {
//                            final NamedHologram holo = HologramDatabase.loadHologram(s);
////                            if (holo.getLine(0) instanceof TextLine && shop.isHoloShop()) {
////                                if (((TextLine) holo.getLine(0)).getText().equals("§a§l" + shop.getName())) {
//                            if (shop.getHolographicShop() == null) {
//                                NamedHologramManager.removeHologram(holo);
//                                holo.delete();
//                                HologramDatabase.deleteHologram(s);
//                                HologramDatabase.saveToDisk();
//
//                                CreateHologram.restoreHoloShop(shop);
//                                System.out.println("Here1");
//
//                            } else {
//                                holo.delete();
//                                System.out.println("Here2");
//                            }
////                                }
////                            }
//                        } catch (Exception e) {
//                            HologramDatabase.deleteHologram(s);
//                            HologramDatabase.saveToDisk();
//
//                            CreateHologram.restoreHoloShop(shop);
//                            System.out.println("Here3");
//                        }
//                    }
//                }
//            }
//            Bukkit.getConsoleSender().sendMessage("§bBetterShops§7 - §aLoaded §d" + HologramManager.getHolographicShops().size() + " §aHologram Shops");

//        }
    }
}
