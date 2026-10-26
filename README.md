# Oxycarbide — All-in-One Server Management

![Minecraft](https://img.shields.io/badge/Minecraft-Java%20Edition-brightgreen)
![Fabric](https://img.shields.io/badge/Fabric-26.3%20%7C%201.21.1%20%7C%201.20.1-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![CurseForge](https://img.shields.io/badge/CurseForge-available%20now-orange)

**English** | [简体中文](README_ZH.md)

Oxycarbide is a server-management mod for modern Minecraft servers, giving players and
admins a large set of practical commands and features.
Most features are enabled by default; login, economy, chunk claims, shops and the global
market start disabled and can be switched on with commands.
Most player-facing prompts can be customised through JSON.
It currently supports Fabric on Minecraft 26.3|26.2|26.1.x|1.21|1.20.

**Note: the mod is still in an early beta and contains AI-generated content, use with caution!**

## Feature overview

| Feature | Default | Enable with |
| --- | --- | --- |
| Homes | on | `/oxycarbide config home switch true` |
| Warps | on | `/oxycarbide config warp switch true` |
| Spawn / back / RTP / TPA | on | `/oxycarbide config spawn\|back\|rtp\|tpa switch true` |
| Nickname / mute / kickme / trashcan | on | `/oxycarbide config nickname\|mute\|kickme\|trashcan switch true` |
| Sweep (incl. smart sweep) | on | `/oxycarbide config sweep switch true` |
| Login | off | `/oxycarbide config login switch true` |
| Economy (coins + special currencies) | off | `/oxycarbide config treasury switch true` |
| Chunk claims | off | `/oxycarbide config claim switch true` |
| Shop GUI | off | `/oxycarbide config shop switch true` |
| Menu GUI | on | `/oxycarbide config menu switch true` |
| Sidebar (scoreboard) | off | `/oxycarbide config scoreboard switch true` |
| Titles | on | `/oxycarbide config titles switch true` |
| Global market | off | `/oxycarbide config market switch true` |

While a feature is disabled, all of its commands answer with "this feature is disabled"
and do nothing. `/oxycarbide config <module>` without arguments shows the current
settings of that module.

Permissions: *admin* means permission level 2 (OP), *everyone* means any player, and
*owner* means the player who created the resource.

Every argument that names an existing object can be filled in with `Tab`: online player
names for `/tpa`, `/mute`, `/addtreasury`, ...; your own home names for `/home` and
`/delhome`; warp names for `/warp`; shop ids for `/shop`; menu ids for `/menu` and
`/menu_default`; listing ids for `/pull`; currency names for `/sell`; `true`/`false` for
every `switch` and rule; the sweep whitelist for `delwhitelist`; title ids for
`/titles get` and the titles you own for `/titles wear`; home names of the whole server for
`/opdelhome`.

## Commands

### Homes

| Command | Permission | Description |
| --- | --- | --- |
| `/sethome <name>` | everyone | Saves the current position as a home. |
| `/home <name>` | everyone | Teleports to one of your homes. |
| `/delhome <name>` | everyone | Deletes one of your homes. |
| `/listhome` | admin | Lists every home of every player on the server. |
| `/opdelhome <home> <player>` | admin | Deletes a home owned by another player, online or offline. |
| `/oxycarbide config home` | everyone | Shows the home settings. |
| `/oxycarbide config home number <amount>` | admin | Home limit for non-admin players, `0` = unlimited (default `0`). |
| `/oxycarbide config home switch <true\|false>` | admin | Enables or disables the home commands. |

### Warps

Warps are server-wide teleport points that remember their creator. When the
`OnlyOp*Warp` rules are off, regular players may create, rename and delete their own
warps.

| Command | Permission | Description |
| --- | --- | --- |
| `/warp <name>` | everyone | Teleports to a warp. |
| `/listwarps` | everyone | Lists every warp and its creator. |
| `/setwarp <name>` | rule-dependent | Creates a warp at your position. |
| `/renamewarp <old> <new>` | rule-dependent | Renames a warp. |
| `/delwarp <name>` | rule-dependent | Deletes a warp. |
| `/oxycarbide config warp number <amount>` | admin | Warp limit per player, `0` = unlimited (default `1`). |
| `/oxycarbide config warp switch <true\|false>` | admin | Enables or disables the warp commands. |
| `/oxycarbide rule OnlyOpCreateWarp <true\|false>` | admin | Only operators may create warps, default `true`. |
| `/oxycarbide rule OnlyOpEditWarp <true\|false>` | admin | Only operators may rename warps, default `true`. |
| `/oxycarbide rule OnlyOpDeleteWarp <true\|false>` | admin | Only operators may delete warps, default `true`. |
| `/oxycarbide rule` | admin | Shows every rule. |

### Travel

| Command | Permission | Description |
| --- | --- | --- |
| `/spawn` | everyone | Teleports to the world spawn. |
| `/back` | everyone | Returns to the position before your last teleport, or your death spot. |
| `/rtp` | everyone | Randomly teleports you to a safe spot on land after a countdown. |
| `/tpa <player>` | everyone | Requests to teleport to that player; they get clickable accept/deny buttons. |
| `/tpahere <player>` | everyone | Requests that player to teleport to you. |
| `/tpaccept` / `/tpadeny` | everyone | Accepts or denies the pending request. |
| `/telepoint_last` | admin | Teleports to the last recorded position of a player. |
| `/oxycarbide config spawn switch <true\|false>` | admin | Enables or disables `/spawn`. |
| `/oxycarbide config back switch <true\|false>` | admin | Enables or disables `/back`. |
| `/oxycarbide config rtp switch <true\|false>` | admin | Enables or disables `/rtp`. |
| `/oxycarbide config rtp radius <blocks>` | admin | Minimum random-teleport distance, default `1000`. |
| `/oxycarbide config rtp countdown <ticks>` | admin | Countdown before teleporting, default `100` (5 s). |
| `/oxycarbide config rtp cooldown <ticks>` | admin | Per-player cooldown, default `600` (30 s). |
| `/oxycarbide config rtp autoprotectnum <amount>` | admin | Simultaneous request threshold for the protection, default `3`. |
| `/oxycarbide rule rtpAutoProtect <true\|false>` | admin | Only lets the first random teleport through, default `true`; disabling it may cause lag. |
| `/oxycarbide config tpa switch <true\|false>` | admin | Enables or disables TPA. |
| `/oxycarbide config tpa countdown <ticks>` | admin | Delay between accepting and teleporting, default `60`. |
| `/oxycarbide config tpa cooldown <ticks>` | admin | Cooldown between requests, default `100`. |
| `/oxycarbide config tpa autodeny <ticks>` | admin | How long a request waits before it is denied, default `100`. |

### Players and chat

| Command | Permission | Description |
| --- | --- | --- |
| `/hat` | everyone | Puts the item in your main hand onto your head (one item at a time). |
| `/nickname <nickname>` | everyone | Changes your own chat nickname. |
| `/nickfor <player> <nickname>` | admin | Changes another player's chat nickname. |
| `/mute <player>` | admin | Mutes a player for the default duration. |
| `/fmute <player>` | admin | Permanently mutes a player; only `/unmute` lifts it. |
| `/unmute <player>` | admin | Lifts a mute. |
| `/kickme` | everyone | Disconnects you, useful when you are stuck. |
| `/invsee <player>` | admin | Opens the target's inventory in a chest menu. |
| `/trashcan` | everyone | Opens a temporary trash can; everything inside is deleted on close. |
| `/oxycarbide config nickname switch <true\|false>` | admin | Enables or disables nicknames. |
| `/oxycarbide config mute switch <true\|false>` | admin | Enables or disables mutes. |
| `/oxycarbide config mute timer <ticks>` | admin | Default `/mute` duration, default `1200` (1 min). |
| `/oxycarbide config kickme switch <true\|false>` | admin | Enables or disables `/kickme`. |
| `/oxycarbide config trashcan switch <true\|false>` | admin | Enables or disables `/trashcan`. |

### Login

When enabled, players that have not logged in are frozen (they cannot move, interact,
break blocks or use items) and are kicked after the timeout.

| Command | Permission | Description |
| --- | --- | --- |
| `/login <password>` | everyone | Simple mode: logs in, or registers on first use. |
| `/login <username> <password>` | everyone | Normal mode: logs in, or registers on first use. |
| `/loginedit <old user> <old pass> <new user> <new pass>` | admin | Changes a login in normal mode. |
| `/loginedit <player> <old pass> <new pass>` | admin | Changes a login in simple mode. |
| `/loginrequest` | admin | Lists login requests since this server start. |
| `/loginlist` | admin | Lists every registered account. |
| `/oxycarbide config login switch <true\|false>` | admin | Enables or disables the login system, default off. |
| `/oxycarbide config login mode <normal\|simple>` | admin | Normal mode (username + password) or simple mode (password only). |
| `/oxycarbide config login time_first <ticks>` | admin | First-login timeout, default `1200`. |
| `/oxycarbide config login time <ticks>` | admin | Timeout for later logins, default `600`. |

### Economy

Coins are the base currency and their name is configurable; special currencies have an
exchange rate against coins.

| Command | Permission | Description |
| --- | --- | --- |
| `/treasury` | everyone | Shows your coin balance and every special currency balance. |
| `/transfer_treasury <player> <amount>` | everyone | Transfers coins to another player. |
| `/addtreasury <player> <amount>` | admin | Adds coins to a player. |
| `/cutreasury <player> <amount>` | admin | Force-deducts coins; the balance may go negative. |
| `/listreasury` | admin | Lists every account (including zero balances) and special currencies. |
| `/sptreasury` | everyone | Lists every special currency and its rate. |
| `/sptreasury create <name> <rate>` | admin | Creates a special currency; the rate is how many coins one unit is worth. |
| `/add_sptreasury <currency> <player> <amount>` | admin | Gives special currency to a player. |
| `/cut_sptreasury <currency> <player> <amount>` | admin | Force-deducts special currency; may go negative. |
| `/transfer_sptreasury <currency> <player> <amount>` | everyone | Transfers special currency. |
| `/oxycarbide config treasury switch <true\|false>` | admin | Enables or disables the economy, default off. |
| `/oxycarbide config treasury translimit <amount>` | admin | Per-transfer limit, `0` = unlimited. |
| `/oxycarbide config treasury currencyname <name>` | admin | Base currency name, default `金币`. |
| `/oxycarbide rule allowPlayerTransTreasury <true\|false>` | admin | Allows player-to-player coin transfers, default `true`. |
| `/oxycarbide rule allowSpecialCurrency <true\|false>` | admin | Enables special currencies, default `false`. |
| `/oxycarbide rule allowPlayerTranSPTreasury <true\|false>` | admin | Allows player-to-player special currency transfers, default `true`. |

### Shops and global market

Shops are JSON-driven chest GUIs (buy, sell, redirect, decoration); the global market is
a paginated GUI of player listings. Both layouts are edited under `config/oxycarbide/`,
see [Shop GUI JSON](#shop-gui-json).

| Command | Permission | Description |
| --- | --- | --- |
| `/shop` | everyone | Opens the default shop. |
| `/shop <id>` | everyone | Opens the given shop. |
| `/shoplist` | admin | Lists every loaded shop. |
| `/shopreload` | admin | Reloads the shop files. |
| `/shop_create <name> <id>` | admin | Creates an empty shop; use `~` for the id to generate one. |
| `/shop_settings default <id>` | admin | Sets the shop opened by a bare `/shop`. |
| `/globalshop` | everyone | Opens the global market. |
| `/sell <currency> <price> <minutes>` | everyone | Lists the held item; `normal` means coins, and `~` uses the default duration. |
| `/pull <id>` | listing owner | Withdraws one of your listings. |
| `/spull <id>` | admin | Force-removes any listing. |
| `/oxycarbide config shop switch <true\|false>` | admin | Enables or disables shops, default off. |
| `/oxycarbide config shop default_time <minutes>` | admin | Duration used by `~` in `/sell`, default `180`. |
| `/oxycarbide config market switch <true\|false>` | admin | Enables or disables the global market, default off. |

### Menus

Menus are JSON-driven chest GUIs: every button either runs a command as the clicking
player or opens another menu page. The files live in `config/oxycarbide/menu/`, see
[Menu GUI JSON](#menu-gui-json).

| Command | Permission | Description |
| --- | --- | --- |
| `/menu` | everyone | Opens the default menu. |
| `/menu <id>` | everyone | Opens the given menu. |
| `/menu_default <id>` | admin | Sets the menu opened by a bare `/menu`. |
| `/menu_create [id]` | admin | Creates an empty menu; `~` (or no id) generates a random id. |
| `/menu_list` | admin | Lists every loaded menu. |
| `/menu_reload` | admin | Reloads the menu files. |
| `/oxycarbide config menu switch <true\|false>` | admin | Enables or disables menus, default on. |

The file name is the menu id; the first start writes `example.json`, which is a good file
to copy. Buttons run with the permissions of the player that clicks them, so a menu can
never give a normal player more rights than they already have. While menus are disabled,
`/menu` answers "this feature is disabled"; the file management commands
(`/menu_create`, `/menu_default`, `/menu_list`, `/menu_reload`) keep working.

### Sidebar

The sidebar is a per-player scoreboard built from
`config/oxycarbide/scoreboard/scoreboard.json`; see
[Scoreboard sidebar JSON](#scoreboard-sidebar-json). It is off by default, and it never
creates or changes vanilla `/scoreboard` objectives.

| Command | Permission | Description |
| --- | --- | --- |
| `/oxycarbide scoreboard` | everyone | Shows whether the sidebar is on, its line count and refresh interval. |
| `/oxycarbide scoreboard reload` | admin | Re-reads `scoreboard.json`. |
| `/oxycarbide scoreboard counter set <counter> <player> <value>` | admin | Sets a custom counter used by `custom` lines. |
| `/oxycarbide scoreboard counter add <counter> <player> <amount>` | admin | Adds to a custom counter (a negative number subtracts). |
| `/oxycarbide config scoreboard switch <true\|false>` | admin | Enables or disables the sidebar, default off. |
| `/oxycarbide rule DisplayOfflinePlayerScoreboard <true\|false>` | admin | Also lists offline players in the sidebar lists, default `false`. |

### Titles

A title is a coloured prefix in front of the player name, both in the chat and above the
player's head. Titles are plain JSON (`config/oxycarbide/title/titles.json`, see
[Title JSON](#title-json)) and do not use the vanilla team feature at all: the chat prefix
comes from the display name and the name tag is the entity's custom name.

The commands live under `/titles`, not `/title`: vanilla already owns `/title` for screen
titles, and registering over it would either replace that command or inherit its operator
requirement (which would lock players out of `/titles wear`).

| Command | Permission | Description |
| --- | --- | --- |
| `/titles` | everyone | Shows the title you are wearing. |
| `/titles create <text> <color> <id>` | admin | Creates a title; `~` as id generates one, `text` may be quoted. |
| `/titles get <id> [player]` | admin | Gives the title to you, or to another player. |
| `/titles wear <id>` | everyone | Wears a title you own; only owned ids are suggested. |
| `/titles clear` | everyone | Stops wearing a title. |
| `/oxycarbide config titles switch <true\|false>` | admin | Enables or disables titles, default on. |

Shops can sell titles: a `command` item with `"command": "/titles get vip %player%"` runs
the operator command for the buyer, see [Shop GUI JSON](#shop-gui-json).

### Chunk claims

| Command | Permission | Description |
| --- | --- | --- |
| `/claim` | rule-dependent | Claims the chunk you stand in. |
| `/chunkbelong` | everyone | Shows the owner and settings of the current chunk. |
| `/claim_transfer <player>` | owner | Transfers the current chunk to another player. |
| `/disclaim` | owner | Removes your claim on the current chunk. |
| `/sp_disclaim` | admin | Force-removes the claim on the current chunk. |
| `/claim_settings whitelist add\|remove <player>` | owner | Manages the chunk whitelist. |
| `/claim_settings grounded <true\|false>` | owner | Grounded mode: pushes non-allowed players out of the chunk. |
| `/claim_settings openToTeam <true\|false>` | owner | Teammates may pass freely. |
| `/claim_settings publicopen <true\|false>` | owner | Opens the chunk to everyone. |
| `/forceload` | owner | Force-loads the current chunk. |
| `/forceload_cancle` | owner | Stops force-loading the current chunk. |
| `/forceload_spcancle` | admin | Force-stops force-loading the current chunk. |
| `/oxycarbide config claim switch <true\|false>` | admin | Enables or disables chunk claims, default off. |
| `/oxycarbide config claim value <currency> <amount>` | admin | Claim price, `0` = free, `normal` = coins. |
| `/oxycarbide config claim maxnum <amount>` | admin | Claim limit per player, `0` = unlimited, default `100`. |
| `/oxycarbide config claim forceloadMaxnum <amount>` | admin | Force-load limit, `0` = forbidden, default `20`, never above `maxnum`. |
| `/oxycarbide rule OnlyOpPlayerClaim <true\|false>` | admin | Only operators may claim chunks, default `true`. |

### Sweep

| Command | Permission | Description |
| --- | --- | --- |
| `/sweep` | admin | Clears every dropped item on the server now (monsters too in `entity` mode). |
| `/oxycarbide config sweep switch <true\|false>` | admin | Master switch for sweeping, default on. |
| `/oxycarbide config sweep time <minutes>` | admin | Automatic sweep interval, `0` = off, default `5`. |
| `/oxycarbide config sweep countdown "<list>"` | admin | Countdown seconds, comma separated, default `120,60,30,15,5,4,3,2,1`. |
| `/oxycarbide config sweep mode <simple\|entity>` | admin | `simple` clears items only, `entity` clears monsters too. |
| `/oxycarbide config sweep whitelist <id>` | admin | Adds an item or entity id to the sweep whitelist. |
| `/oxycarbide config sweep delwhitelist <id>` | admin | Removes an id from the whitelist. |
| `/oxycarbide config sweep smartsweep <true\|false>` | admin | Enables smart sweep, default off. |
| `/oxycarbide config smartsweep threshold <tps> <drops>` | admin | Smart-sweep trigger thresholds, default TPS `15`, drops `200`. |
| `/oxycarbide config smartsweep scancountdown <seconds>` | admin | Drop re-scan interval, default `15`. |
| `/oxycarbide config smartsweep sweepcountdown <seconds>` | admin | Minimum time between two smart sweeps, default `10`. |
| `/oxycarbide rule slienceSweep <true\|false>` | admin | Silent sweep: sends no sweep messages, default `false`. |

Smart sweep samples the server tick time itself to estimate TPS. A sweep only triggers
when TPS is below the threshold **and** the drop count reaches the drop threshold, and
it is limited by the scan and trigger cooldowns. Whitelisted ids are never removed in
either mode.

### Display and configuration

| Command | Permission | Description |
| --- | --- | --- |
| `/oxycarbide config promptmode` | everyone | Shows your current prompt position. |
| `/oxycarbide config promptmode dialog` | everyone | Prompts appear in the chat (default). |
| `/oxycarbide config promptmode actionbar` | everyone | Prompts appear as small text just above the hotbar. |
| `/oxycarbide reload` | everyone | Shows what the two reload variants do. |
| `/oxycarbide reload server` | admin | Re-reads `config/oxycarbide.json`, every language file and the shop files. |
| `/oxycarbide reload client` | everyone | Asks your own game to reload its local resources (like `F3 + T`). |

`promptmode` is stored per player (`<world>/oxycarbide_promptmode.json`), does not
affect other players and survives restarts. In `actionbar` mode, every system message sent
to that player (all Oxycarbide prompts, command feedback, join/death notices, ...) is
shown above the hotbar instead of in the chat, in the normal small font, and fades out on
its own after a few seconds. The one exception is a teleport request: `/tpa` and
`/tpahere` always arrive in the chat, because the green **accept** and red **deny**
buttons are clickable text and the action bar cannot be clicked. The old `title` value
(screen centre subtitle) is still accepted as an alias and is converted on load.

`reload server` never needs a restart, so it is the fast way to apply edited texts or
settings; it reloads the settings file, the 19 text files of every language, the shop and
menu files and the per-player prompt modes. `reload client` only touches the game of the
player who ran it, and needs oxycarbide installed on that client as well (a server-only
install answers that the client has nothing to reload).

## Custom messages

All player-facing prompts live in the config directory and are generated on first
start. Only change the **values** in the JSON, not the keys.

## Data files

| Path | Contents |
| --- | --- |
| `config/oxycarbide.json` | All feature settings (also editable in game). |
| `config/oxycarbide/lang/<language>/*_text.json` | Custom messages. |
| `config/oxycarbide/shop/config/<id>.json` | Shop GUIs. |
| `config/oxycarbide/menu/<id>.json` | Menus. |
| `config/oxycarbide/scoreboard/scoreboard.json` | Sidebar layout. |
| `config/oxycarbide/title/titles.json` | Titles. |
| `config/oxycarbide/globalshop_default.json` | Global market decoration layout. |
| `<world>/oxycarbide_homes.json` | Homes. |
| `<world>/oxycarbide_warps.json` | Warps. |
| `<world>/oxycarbide_nicknames.json` | Nicknames. |
| `<world>/oxycarbide_mutes.json` | Mutes. |
| `<world>/oxycarbide_lastplayer.json` | Last player position (`/telepoint_last`). |
| `<world>/oxycarbide_accounts.json` | Login accounts. |
| `<world>/oxycarbide_treasury.json` | Coin accounts. |
| `<world>/oxycarbide_sptreasury.json` | Special currencies. |
| `<world>/oxycarbide_claims.json` | Chunk claims. |
| `<world>/oxycarbide_counters.json` | Custom counters of the sidebar. |
| `<world>/oxycarbide_titles.json` | Owned and worn titles. |
| `<world>/oxycarbide_market.json` | Global market listings. |
| `<world>/oxycarbide_market_pending.json` | Pending market returns. |
| `<world>/oxycarbide_promptmode.json` | Per-player prompt position. |

Back up both the config directory and the files above inside the world save.

## License

This project is available under the MIT license. See [LICENSE](LICENSE) for the full
text. It is based on the Fabric example mod, which is also released under MIT.
