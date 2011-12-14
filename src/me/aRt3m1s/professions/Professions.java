package me.aRt3m1s.professions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Christian
 * Date: 9/15/11
 * Time: 7:28 AM
 * To change this template use File | Settings | File Templates.
 */

@SuppressWarnings("deprecation")
public class Professions extends JavaPlugin{
    private static final Logger log = Logger.getLogger("Minecraft");
    private final Utils utils = new Utils(this);
    private static final String pPrefix = "[PROF]";
    PermissionManager pm;
    File sFile;
    File hFile;
    Configuration settings;
    Configuration history;
    public static Economy economy = null;
    /**
     * Called when this plugin is disabled
     */

    private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
    public void onDisable() {
        log.info(getDescription().getName()+" version v"+getDescription().getVersion()+" is Disabled!");
    }

    /**
     * Called when this plugin is enabled
     */
    @Override
    public void onEnable() {
        pm = PermissionsEx.getPermissionManager();
        sFile = new File(getDataFolder().getPath()+"/settings.yml");
        hFile = new File(getDataFolder().getPath()+"/history.yml");
        firstTimeCheck();
        settings = new Configuration(sFile);history = new Configuration(hFile);
        settings.load();history.load();

        log.info(getServer().getPluginManager().getPlugin("Register").getDescription().getFullName() + " is Enabled!");
        log.info(getDescription().getName()+" version v"+getDescription().getVersion()+" is Enabled!");
        
        if (!setupEconomy()) {
            System.out.println("Null econ");
           //use these if you require econ
          //getServer().getPluginManager().disablePlugin(this);
          //return;
        }
    }

    private void firstTimeCheck() {
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }
        if(!sFile.exists()){
            InputStream inputThis = getClassLoader().getResourceAsStream("settings.yml");
            try{
                utils.copy(inputThis, sFile);
                log.info(pPrefix+" settings.yml is created");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(!hFile.exists()){
            InputStream inputThis = getClassLoader().getResourceAsStream("history.yml");
            try{
                utils.copy(inputThis, hFile);
                log.info(pPrefix+" history.yml is created");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(cmd.getName().equalsIgnoreCase("professions")){
                if(args.length==0){
                    player.sendMessage(ChatColor.GREEN+"Empty Parameters!");
                    player.sendMessage(ChatColor.GREEN+"Params: join,leave,list,me,user");
                    return true;
                }else if(args.length>=1&&args.length<=3){
                    if(args[0].equalsIgnoreCase("join")){
                        if(args.length==1){
                            return this.oneArg(player);
                        }else{
                            if(!this.isProfExists(player, args[1])){
                                return noProfExists(player);
                            }else{
                                if(args.length==2){
                                    return this.twoArg(player, args[1]);
                                }
                                if(args.length==3){
                                    if(!this.isGroupInProfession(player, args)){
                                        player.sendMessage(ChatColor.GREEN + "Group has no/different Profession!");
                                        return true;
                                    }
                                    if(!canUserJoinGroup(player, args)){
                                        player.sendMessage(ChatColor.GREEN + "Unable to join group");
                                        return true;
                                    }else{
                                        String rGname = getOrigGroupName(args[2]);
                                        if(this.isIConomyEnabled()){
                                            double joinCost = settings.getDouble(player.getWorld().getName() + ".professions." + args[1] + ".join-cost", 0.0);
                                            double acc = economy.getBalance(player.getName());
                                            if(acc >= joinCost)
                                            {
                                                pm.getUser(player).addGroup(rGname, player.getWorld().getName());
                                                history.setProperty(player.getWorld().getName() + "." + player.getName() + "." + rGname + ".last-join", System.currentTimeMillis());
                                                history.save();
                                                economy.withdrawPlayer(player.getName(), joinCost);
                                            	player.sendMessage(ChatColor.GREEN + Double.toString(joinCost) + " is subtracted from your holdings");
                                                player.sendMessage(ChatColor.GREEN + "Joined Successfully");
                                            }
                                            else
                                            {
                                            	player.sendMessage(ChatColor.RED + "Not enough money to join!");
                                            }
                                            	
                                        }
                                        else
                                        {
                                            pm.getUser(player).addGroup(rGname, player.getWorld().getName());
                                            history.setProperty(player.getWorld().getName() + "." + player.getName() + "." + rGname + ".last-join", System.currentTimeMillis());
                                            history.save();
                                            player.sendMessage(ChatColor.GREEN + "Joined Successfully");                                        	
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }else if(args[0].equalsIgnoreCase("leave")){
                        if(args.length==1){
                            return this.oneArg(player);
                        }else{
                            if(!this.isProfExists(player, args[1])){
                                return noProfExists(player);
                            }else{
                                if(args.length==2){
                                    return this.twoArg(player, args[1]);
                                }
                                if(args.length==3){
                                    if(!this.isGroupInProfession(player, args)){
                                        player.sendMessage(ChatColor.GREEN + "Group has no/different Profession!");
                                        return true;
                                    }
                                    if(!canUserLeaveGroup(player, args)){
                                        player.sendMessage(ChatColor.GREEN+"Unable to leave group!");
                                        return true;
                                    }else{
                                        String rGname = getOrigGroupName(args[2]);
                                        if(this.isIConomyEnabled()){
                                            double leaveCost = settings.getDouble(player.getWorld().getName() + ".professions." + args[1] + ".leave-cost", 0.0);
                                            double acc = economy.getBalance(player.getName());
                                            if(acc >= leaveCost)
                                            {
                                                pm.getUser(player).removeGroup(rGname);
                                                history.setProperty(player.getWorld().getName() + "." + player.getName() + "." + rGname + ".last-join", 0);
                                                history.save();
                                                economy.withdrawPlayer(player.getName(), leaveCost);
                                            	player.sendMessage(ChatColor.GREEN + Double.toString(leaveCost) + " is subtracted from your holdings");
                                                player.sendMessage(ChatColor.GREEN + "Leaved Successfully");
                                            }
                                            else
                                            {
                                            	player.sendMessage(ChatColor.RED + "Not enough money to join!");
                                            }
                                        }
                                        else
                                        {
                                        	pm.getUser(player).removeGroup(rGname);
                                            history.setProperty(player.getWorld().getName() + "." + player.getName() + "." + rGname + ".last-join", 0);
                                            history.save();
                                            player.sendMessage(ChatColor.GREEN + "Leaved Successfully");                                        	
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }else if(args[0].equalsIgnoreCase("list")){
                        if(args.length==1){
                            List<String> availableProf = settings.getKeys(player.getWorld().getName() + ".professions");
                            String compacted = availableProf.toString().substring(1, availableProf.toString().length()-1);
                            player.sendMessage(ChatColor.GREEN+"Available Professions:");
                            player.sendMessage(ChatColor.GREEN+compacted);
                            return true;
                        }
                        if(args.length==2){
                            String prof = args[1];
                            PermissionGroup[] allGroups = pm.getGroups();
                            String groupsHasProfession = "";
                            int counter = 0;
                            while(counter<allGroups.length){
                                if(allGroups[counter].getOwnOption("profession").equalsIgnoreCase(prof)){
                                    groupsHasProfession += allGroups[counter].getName()+", ";
                                }
                                counter++;
                            }
                            if(groupsHasProfession.isEmpty()){
                                player.sendMessage(ChatColor.GREEN+"No Groups in profession '"+prof+"'");
                                return true;
                            }else{
                                player.sendMessage(ChatColor.GREEN+"Available Professions:");
                                player.sendMessage(ChatColor.GREEN+groupsHasProfession.substring(0, groupsHasProfession.length()-2));
                                return true;
                            }
                        }
                        if(args.length>=3){
                            player.sendMessage(ChatColor.GREEN+"Too many Parameters!");
                            return true;
                        }
                    }else if(args[0].equalsIgnoreCase("me")){
                        return checkUserProfession(player, player.getName());
                    }else if(args[0].equalsIgnoreCase("user")){
                        return checkUserProfession(player, args[1]);
                    }else if((args[0].equalsIgnoreCase("help"))||(args[0].equalsIgnoreCase("?"))){
                        return false;
                    }else{
                        player.sendMessage(ChatColor.GREEN+"Invalid Parameters!");
                        player.sendMessage(ChatColor.GREEN+"Params: join,leave,list,me,user");
                        return true;
                    }
                }else{
                    player.sendMessage(ChatColor.GREEN+"Too many Parameters!");
                    return true;
                }
            }else if(cmd.getName().equalsIgnoreCase("settings")){
                if(player.hasPermission("professions.settings")||player.isOp()){
                if(args.length==0){
                    player.sendMessage(ChatColor.GREEN+"Empty Parameters!");
                    player.sendMessage(ChatColor.GREEN+"Params: addProfession,limit,time,joincost,leavecost");
                    return true;
                }else if(args.length>=1&&args.length<=3){
                    if(args[0].equalsIgnoreCase("addProfession")){
                        if(args.length==1){
                            player.sendMessage(ChatColor.GREEN+"Specify a Profession!");
                            return true;
                        }else{
                            settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".limit",
                                    settings.getInt("settings.default-limit", 1));
                            settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".time",
                                    settings.getInt("settings.default-time", 1));
                            settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".join-cost",
                                    settings.getInt("settings.default-joincost", 1));
                            settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".leave-cost",
                                    settings.getInt("settings.default-leavecost", 1));
                            settings.save();
                            player.sendMessage(ChatColor.GREEN+"Profession added!");
                            return true;
                        }
                    }else if(args[0].equalsIgnoreCase("limit")){
                        if(args.length==1||args.length==2){
                            return this.emptyPSett(player, args);
                        }else{
                            try{
                                int limit = Integer.parseInt(args[2]);
                                if(settings.getKeys(player.getWorld().getName()+".professions").contains(args[1])){
                                    settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".limit", limit);
                                    settings.save();
                                    player.sendMessage(ChatColor.GREEN+"Limit is changed");
                                    return true;
                                }else{
                                    player.sendMessage(ChatColor.GREEN+"No such Professions");
                                    return true;
                                }
                            }catch(NumberFormatException e){
                                player.sendMessage(ChatColor.GREEN+"Value is not a Number!");
                                return true;
                            }
                        }
                    }else if(args[0].equalsIgnoreCase("time")){
                        if(args.length==1||args.length==2){
                            return this.emptyPSett(player, args);
                        }else{
                            try{
                                int time = Integer.parseInt(args[2]);
                                if(settings.getKeys(player.getWorld().getName()+".professions").contains(args[1])){
                                    settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".time", time);
                                    settings.save();
                                    player.sendMessage(ChatColor.GREEN+"Time is changed");
                                    return true;
                                }else{
                                    player.sendMessage(ChatColor.GREEN+"No such Professions");
                                    return true;
                                }
                            }catch(NumberFormatException e){
                                player.sendMessage(ChatColor.GREEN+"Value is not a Number!");
                                return true;
                            }

                        }
                    }else if(args[0].equalsIgnoreCase("joincost")){
                        if(args.length==1||args.length==2){
                            return this.emptyPSett(player, args);
                        }else{
                            try{
                                double jCost = Double.parseDouble(args[2]);
                                if(settings.getKeys(player.getWorld().getName()+".professions").contains(args[1])){
                                    settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".join-cost", jCost);
                                    settings.save();
                                    player.sendMessage(ChatColor.GREEN+"Join-cost is changed");
                                    return true;
                                }else{
                                    player.sendMessage(ChatColor.GREEN+"No such Professions");
                                    return true;
                                }
                            }catch(NumberFormatException e){
                                player.sendMessage(ChatColor.GREEN+"Value is not a Number!");
                                return true;
                            }

                        }
                    }else if(args[0].equalsIgnoreCase("leavecost")){
                        if(args.length==1||args.length==2){
                            return this.emptyPSett(player, args);
                        }else{
                            try{
                                double lCost = Double.parseDouble(args[2]);
                                if(settings.getKeys(player.getWorld().getName()+".professions").contains(args[1])){
                                    settings.setProperty(player.getWorld().getName()+".professions."+args[1]+".leave-cost", lCost);
                                    settings.save();
                                    player.sendMessage(ChatColor.GREEN+"Leave-cost is changed");
                                    return true;
                                }else{
                                    player.sendMessage(ChatColor.GREEN+"No such Professions");
                                    return true;
                                }
                            }catch(NumberFormatException e){
                                player.sendMessage(ChatColor.GREEN+"Value is not a Number!");
                                return true;
                            }

                        }
                    }else if(args[0].equalsIgnoreCase("reload")){
                        settings.load();
                        history.load();
                        return true;
                    }else if(args[0].equalsIgnoreCase("help")||args[0].equalsIgnoreCase("?")){
                        return false;
                    }
                }else{
                    player.sendMessage(ChatColor.GREEN+"Too many Parameters!");
                    return true;
                }
                }else{
                    player.sendMessage(ChatColor.RED+"You have no permission to use that!");
                    return true;
                }
            }
        }else{
            sender.sendMessage("Consoles can not use this commands!");
            return true;
        }
        return false;
    }

    private boolean emptyPSett(Player player, String[] args) {
        if(args.length==1){
            player.sendMessage(ChatColor.GREEN+"Specify a Profession!");
            return true;
        }else{
            player.sendMessage(ChatColor.GREEN+"Specify a Value!");
            return true;
        }
    }

    private boolean checkUserProfession(Player player, String name) {
        PermissionGroup[] usersGroups = pm.getUser(name).getGroups(player.getWorld().getName());
        int counter = 0;
        player.sendMessage((ChatColor.RED+"GROUP")+(ChatColor.WHITE+"<==>")+(ChatColor.RED+"PROFESSION"));
        while(counter<usersGroups.length){
            String group = usersGroups[counter].getName();
            String prof = usersGroups[counter].getOwnOption("profession");
            if(prof.isEmpty()){
                prof = "NoProfessionS";
            }
            player.sendMessage((ChatColor.GREEN+group)+(ChatColor.WHITE+"<==>")+(ChatColor.GREEN+prof));
            counter++;
        }
        return true;
    }

    private boolean canUserLeaveGroup(Player player, String[] args) {
        String rGname = this.getOrigGroupName(args[2]);
        int time = settings.getInt(player.getWorld().getName()+".professions."+args[1]+".time", 1);
        long lastJoin = Long.parseLong(history.getString(player.getWorld().getName()+"."+player.getName()+"."+
                rGname+".last-join", "0"));
        if(pm.getUser(player).inGroup(rGname, player.getWorld().getName())){
            if(((time*3600000)+lastJoin)<System.currentTimeMillis()){
                return true;
            }else{
                player.sendMessage(ChatColor.GREEN+"Time is not up yet!");
                return false;
            }
        }else{
            player.sendMessage(ChatColor.GREEN+"You are not in that group!");
            return false;
        }
    }

    private boolean twoArg(Player player, String prof) {
        player.sendMessage(ChatColor.GREEN+"Empty Parameters!");
        player.sendMessage(ChatColor.GREEN+"Type '/p list "+prof+"' for Available Groups!");
        return true;
    }

    private boolean isIConomyEnabled() {
        return settings.getBoolean("settings.enable-iConomy", false);
    }

    private String getOrigGroupName(String group) {
        return pm.getGroup(group).getName();
    }

    private boolean canUserJoinGroup(Player player, String[] args) {
        String group = args[2];
        String prof = args[1];
        String[] usersGroups = pm.getUser(player).getGroupsNames(player.getWorld().getName());
        int limit = settings.getInt(player.getWorld().getName()+".professions."+prof+".limit", 1);
        int count = this.usersGroupsInProfession(player, prof);
        boolean userHasRequirement = this.doesUserHasRequirement(player, group);
        boolean inGroup = false;
        for(String g: usersGroups){
            if(g.equalsIgnoreCase(group)){
                inGroup = true;
                break;
            }
            inGroup = false;
        }
        if(inGroup){
            player.sendMessage(ChatColor.GREEN + "You are already in that group");
            return false;
        }
        if(limit>count){
            if(userHasRequirement){
                return true;
            }else{
                player.sendMessage(ChatColor.GREEN + "You don't have enough requirements to join that group");
                return false;
            }
        }else{
            player.sendMessage(ChatColor.GREEN + "Joining that group will exceed the professions limit number");
            return false;
        }
    }

    private boolean doesUserHasRequirement(Player player, String group) {
        boolean userHasRequirement = false;
        String[] usersGroups = pm.getUser(player).getGroupsNames(player.getWorld().getName());
        String requirements = pm.getGroup(group).getOwnOption("profession-requirement", player.getWorld().getName());
        String[] reqSplit = requirements.split(",");
        if(requirements.isEmpty()){
            return true;
        }
        for(String rS: reqSplit){
            String[] rSS = rS.split("&");
            for(String g: rSS){
                int counter = 0;
                while(counter<usersGroups.length){
                    if(usersGroups[counter].equalsIgnoreCase(g)){
                        userHasRequirement = true;
                        break;
                    }else{
                        userHasRequirement = false;
                        counter++;
                    }
                }
                if(!userHasRequirement){
                    break;
                }
            }
            if(userHasRequirement){
                break;
            }
        }
        return userHasRequirement;
    }

    private int usersGroupsInProfession(Player player, String prof) {
        PermissionGroup[] usersGroups = pm.getUser(player).getGroups(player.getWorld().getName());
        int count = 0;
        int counter = 0;
        while(counter<usersGroups.length){
            if(usersGroups[counter].getOwnOption("profession", player.getWorld().getName()).equalsIgnoreCase(prof)){
                count++;
            }
            counter++;
        }
        return count;
    }

    private boolean noProfExists(Player player) {
        player.sendMessage(ChatColor.GREEN + "There is no such Profession!");
        player.sendMessage(ChatColor.GREEN + "Type /p list for available Professions!");
        return true;
    }

    private boolean isGroupInProfession(Player player, String[] args) {
        String group = args[2];
        String prof = args[1];
        return pm.getGroup(group).getOwnOption("profession", player.getWorld().getName()).equalsIgnoreCase(prof);
    }

    private boolean isProfExists(Player player, String profession) {
        List<String> allProfs = settings.getKeys(player.getWorld().getName()+".professions");
        return allProfs.contains(profession);
    }

    private boolean oneArg(Player player) {
        player.sendMessage(ChatColor.GREEN+"Empty Parameters, please specify a Profession!");
        player.sendMessage(ChatColor.GREEN+"Type '/p list' for available Professions");
        return true;
    }
}
