# Gacha+



# Installation method

Please put jar in the plugins folder of spigot or bukkit.   

Note: It is recommended that you use a protection plug-in such as LWC or WorldGuard.  

# Useage

1. Create signbord.  
 line1 `[gachaPlus]` Fixed character string  
 line2 `gacha1` Specify gachaPlus name. Please enter the this line with one-byte alphanumeric underscore  
 line3 `***gacha1***` Title for sign.  
 line4 `test gachaPlus` Comment for sign.  

2. Set Chest.
 /gachaPlus modify gacha1  

 Please right click and punch the chest.  
 After setting the chest, it is completed.  
 Hold the ticket on your right hand and right click on the signbord to receive the prize.  

3. Command.

```
/gachaPlus list - List all gachaPlus
/gachaPlus modify <gacha_name> - Modify a gachaPlus
/gachaPlus delete <gacha_name> - Delete a gachaPlus
/gachaPlus ticket @a - Tickets will be issued to all players.
/gachaPlus ticket @p - We will issue a ticket to the nearest player.
/gachaPlus ticket @r - One person will be randomly selected from the players and a ticket will be issued.
/gachaPlus ticket @s - Issue a ticket to the player who executed the command
/gachaPlus ticket <player_name> - Issue a ticket
```

# Permission

```
gachaPlus.list - /gachaPlus list
gachaPlus.modify - /gachaPlus modify
gachaPlus.delete - /gachaPlus delete
gachaPlus.create - create gachaPlus by signbord
```

Other operation is op.
Issue ticket is console or op.