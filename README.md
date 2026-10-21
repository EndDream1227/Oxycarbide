# Oxycarbide — All-in-One Server Management

**English** | [简体中文](README_ZH.md)

Oxycarbide is a server-management mod for modern Minecraft servers, giving players and
admins a large set of practical commands and features.
Most features are enabled by default; login, economy, chunk claims, shops and the global
market start disabled and can be switched on with commands.
Most player-facing prompts can be customised through JSON.
It currently supports Fabric on Minecraft 26.3.

### Note: the mod is still in an early beta and contains AI-generated content, use with caution!

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
| Global market | off | `/oxycarbide config market switch true` |

While a feature is disabled, all of its commands answer with "this feature is disabled"
and do nothing. `/oxycarbide config <module>` without arguments shows the current
settings of that module.

Permissions: *admin* means permission level 2 (OP), *everyone* means any player, and
*owner* means the player who created the resource.

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
| `/oxycarbide config promptmode title` | everyone | Prompts appear as a subtitle in the middle of the screen. |

`promptmode` is stored per player (`<world>/oxycarbide_promptmode.json`), does not
affect other players and survives restarts. In `title` mode, every system message sent
to that player (all Oxycarbide prompts, command feedback, join/death notices, ...) is
shown as a subtitle; messages with clickable buttons (e.g. the TPA accept/deny buttons)
lose their click action, so `dialog` is recommended for those.

## Custom messages

All player-facing prompts live in the config directory and are generated on first
start. Only change the **values** in the JSON, not the keys.

### Where the files live

```
config/oxycarbide/lang/zh/<name>_text.json   # Chinese
config/oxycarbide/lang/en/<name>_text.json   # English
```

The server picks the Chinese files when the JVM default locale starts with `zh`,
otherwise the English ones. The two sets are independent, so keep both up to date.

| File | Contents |
| --- | --- |
| `home_text.json` | home set/teleport/delete prompts |
| `warp_text.json` | warp create/teleport/rename/permission prompts |
| `spawn_text.json` | `/spawn` prompt |
| `back_text.json` | `/back` prompts |
| `rtp_text.json` | random teleport prompts |
| `tpa_text.json` | teleport request prompts |
| `hat_text.json` | `/hat` prompts |
| `nickname_text.json` | nickname prompts |
| `mute_text.json` | mute prompts |
| `login_text.json` | login/registration prompts |
| `treasury_text.json` | coin prompts |
| `sptreasury_text.json` | special currency transfer prompts |
| `claim_text.json` | chunk claim prompts |
| `shop_text.json` | shop buy/sell prompts |
| `market_text.json` | global market prompts |
| `sweep_text.json` | sweep prompts |

### How to edit

1. Start the server once to generate `config/oxycarbide/lang/`;
2. open the file with a UTF-8 editor (for example
   `config/oxycarbide/lang/zh/home_text.json`);
3. change only the text after the colon, keeping the keys, quotes and commas intact;
4. restart the server (the files are read at startup; there is no reload command).

### Placeholders

`%s` is replaced with a runtime argument (player name, amount, coordinates, ...), and
`%%` prints a literal percent sign. **The number and order of placeholders must match
the defaults**, otherwise arguments will be misplaced or missing:

```json
{
  "set": "Home \"%s\" has been set at %s."
}
```

In that entry the first `%s` is the home name and the second is the coordinates (the
coordinates are clickable and fill in a `/tp` command). You can reword it freely, but
both `%s` must stay.

The `<username>` and `<password>` in the login files are plain text, not placeholders.

### Fallbacks

- missing file: the built-in default text is used;
- missing key: that single message falls back to its default, other keys are unaffected;
- broken JSON (trailing comma, comments, unbalanced quotes): the file fails to load, a
  warning is logged and the defaults are used for the whole file; the server still
  starts normally.

## Shop GUI JSON

Each shop is one file:

```
config/oxycarbide/shop/config/<id>.json
```

The file name without `.json` is the shop id. Recommended flow:
`/shop_create "Main shop" ~` → edit the file → `/shopreload` → `/shop <id>` →
`/shop_settings default <id>`.

### Structure

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| `title` | string | file name | Window title. |
| `rows` | int | `6` | Chest rows, 1–6. |
| `items` | array | `[]` | Clickable entries. |

`row` and `column` are 1-based; the slot is `(row-1)*9 + (column-1)`:

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| `row` / `column` | int | `1` | Position. |
| `display` | string | `item` | Item id drawn in the slot. |
| `enchanted` | bool | `false` | Adds the enchantment glint. |
| `name` | string | none | Display name. |
| `lore` | string array | none | One description line per element. |
| `type` | string | `placeholder` | `buy`, `sell`, `redirect` or `placeholder`. |
| `item` | string | none | Traded item id (required for buy/sell). |
| `amount` | int | `1` | Items moved per trade. |
| `price` | long | `0` | Money moved per trade, `0` = free. |
| `currency` | string | `normal` | `normal` = coins, otherwise a special currency name. |
| `target` | string | none | Shop id opened by `redirect`. |

Unknown `type` values fall back to `placeholder`; a wrong `display`/`item` id falls back
to `minecraft:stone` so a typo cannot break the menu. Only the left mouse button
triggers a trade; right click, shift click and dragging are ignored, and items can never
be taken out of the GUI.

### Example

```json
{
  "title": "Main shop",
  "rows": 6,
  "items": [
    {
      "row": 2, "column": 2,
      "display": "minecraft:diamond",
      "enchanted": true,
      "name": "Diamond",
      "lore": ["Left click to buy one"],
      "type": "buy",
      "item": "minecraft:diamond",
      "amount": 1,
      "price": 100
    },
    {
      "row": 2, "column": 4,
      "display": "minecraft:emerald",
      "name": "Emerald",
      "lore": ["Left click to sell one"],
      "type": "sell",
      "item": "minecraft:emerald",
      "amount": 1,
      "price": 50
    },
    {
      "row": 2, "column": 8,
      "display": "minecraft:compass",
      "name": "Next page",
      "type": "redirect",
      "target": "other_shop_id"
    }
  ]
}
```

## Global market layout

The top and bottom rows of the `/globalshop` GUI are decoration and can be changed in
`config/oxycarbide/globalshop_default.json`:

```json
{
  "title": "Global market",
  "rows": 6,
  "previous": { "display": "minecraft:paper", "name": "Previous" },
  "next": { "display": "minecraft:paper", "name": "Next" },
  "frame": [
    { "row": 1, "column": 1, "display": "minecraft:black_stained_glass_pane", "type": "placeholder" },
    { "row": 6, "column": 2, "display": "minecraft:compass", "name": "Back to shop", "type": "redirect", "target": "shop1" }
  ]
}
```

`frame` entries only support `redirect` and `placeholder`; any other type is forced to
`placeholder`. Listings occupy the rows between the top and bottom frames. Listing,
withdrawing and expiry all keep the full item data; when the seller is offline the item
is queued and delivered automatically on their next join.

## Data files

| Path | Contents |
| --- | --- |
| `config/oxycarbide.json` | All feature settings (also editable in game). |
| `config/oxycarbide/lang/zh\|en/*_text.json` | Custom messages. |
| `config/oxycarbide/shop/config/<id>.json` | Shop GUIs. |
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
| `<world>/oxycarbide_market.json` | Global market listings. |
| `<world>/oxycarbide_market_pending.json` | Pending market returns. |
| `<world>/oxycarbide_promptmode.json` | Per-player prompt position. |

Back up both the config directory and the files above inside the world save.

## License

This project is available under the MIT license. See [LICENSE](LICENSE) for the full
text. It is based on the Fabric example mod, which is also released under MIT.
