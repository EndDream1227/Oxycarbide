# Oxycarbide 一站式服务器管理

[English](README.md) | **简体中文**

Oxycarbide 是面向高版本 Minecraft 服务端的服务器管理模组，为玩家和管理员提供了许多实用指令和功能。
多数功能默认开启，登录、经济、区块申领、商店、全球市场默认关闭，需要时用指令打开。
大部分面向玩家的提示都可以通过 JSON 自定义。
现兼容Fabric MC26.3版本。

### 提醒：mod暂处于早期beta版本，且包含ai生成内容，请谨慎使用！

## 功能开关一览

| 功能 | 默认 | 开启指令 |
| -- | --- | --- |
| 家 | 开 | `/oxycarbide config home switch true` |
| 地标 | 开 | `/oxycarbide config warp switch true` |
| 出生点 / 返回 / 随机传送 / TPA | 开 | `/oxycarbide config spawn\|back\|rtp\|tpa switch true` |
| 昵称 / 禁言 / 主动退出 / 垃圾桶 | 开 | `/oxycarbide config nickname\|mute\|kickme\|trashcan switch true` |
| 清扫（含智能清扫） | 开 | `/oxycarbide config sweep switch true` |
| 登录 | 关 | `/oxycarbide config login switch true` |
| 经济（金币 + 特殊货币） | 关 | `/oxycarbide config treasury switch true` |
| 区块申领 | 关 | `/oxycarbide config claim switch true` |
| 商店 GUI | 关 | `/oxycarbide config shop switch true` |
| 全球市场 | 关 | `/oxycarbide config market switch true` |

关闭某个功能后，它的全部指令都会回复“该功能已关闭”，不会执行任何操作。各模块执行
`/oxycarbide config <模块>`（不带参数）可以查看当前配置。

权限说明：`管理员` 指权限等级 2（OP），`所有人` 指任意玩家，`所有者` 指资源创建者。

## 指令

### 家

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/sethome <名称>` | 所有人 | 把当前位置保存为家。 |
| `/home <名称>` | 所有人 | 传送到自己的家。 |
| `/delhome <名称>` | 所有人 | 删除自己的家。 |
| `/listhome` | 管理员 | 列出全服所有玩家的家。 |
| `/opdelhome <家> <玩家>` | 管理员 | 删除任意玩家（可离线）的家。 |
| `/oxycarbide config home` | 所有人 | 查看家园配置。 |
| `/oxycarbide config home number <数量>` | 管理员 | 非管理员的家数量上限，`0` 为不限，默认 `0`。 |
| `/oxycarbide config home switch <true\|false>` | 管理员 | 家园功能开关。 |

### 地标

地标是全服共享的传送点，并记录创建者。`OnlyOp*Warp` 规则关闭时，普通玩家可以创建、
重命名、删除自己的地标。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/warp <名称>` | 所有人 | 传送到地标。 |
| `/listwarps` | 所有人 | 列出全部地标及创建者。 |
| `/setwarp <名称>` | 视规则 | 在当前位置创建地标。 |
| `/renamewarp <旧名> <新名>` | 视规则 | 重命名地标。 |
| `/delwarp <名称>` | 视规则 | 删除地标。 |
| `/oxycarbide config warp number <数量>` | 管理员 | 每名玩家可创建的地标上限，`0` 为不限，默认 `1`。 |
| `/oxycarbide config warp switch <true\|false>` | 管理员 | 地标功能开关。 |
| `/oxycarbide rule OnlyOpCreateWarp <true\|false>` | 管理员 | 只有管理员能创建，默认 `true`。 |
| `/oxycarbide rule OnlyOpEditWarp <true\|false>` | 管理员 | 只有管理员能重命名，默认 `true`。 |
| `/oxycarbide rule OnlyOpDeleteWarp <true\|false>` | 管理员 | 只有管理员能删除，默认 `true`。 |
| `/oxycarbide rule` | 管理员 | 查看全部规则。 |

### 传送

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/spawn` | 所有人 | 传送到世界出生点。 |
| `/back` | 所有人 | 返回上一次传送前的位置或死亡地点。 |
| `/rtp` | 所有人 | 倒计时后随机传送到安全的陆地位置。 |
| `/tpa <玩家>` | 所有人 | 请求传送到对方身边，对方聊天栏会出现同意/拒绝按钮。 |
| `/tpahere <玩家>` | 所有人 | 请求对方传送到你身边。 |
| `/tpaccept` / `/tpadeny` | 所有人 | 同意 / 拒绝待处理的传送请求。 |
| `/telepoint_last` | 管理员 | 传送到最近一次记录到的玩家位置。 |
| `/oxycarbide config spawn switch <true\|false>` | 管理员 | `/spawn` 开关。 |
| `/oxycarbide config back switch <true\|false>` | 管理员 | `/back` 开关。 |
| `/oxycarbide config rtp switch <true\|false>` | 管理员 | `/rtp` 开关。 |
| `/oxycarbide config rtp radius <格数>` | 管理员 | 随机传送的最小距离，默认 `1000`。 |
| `/oxycarbide config rtp countdown <tick>` | 管理员 | 传送前倒计时，默认 `100`（5 秒）。 |
| `/oxycarbide config rtp cooldown <tick>` | 管理员 | 每名玩家的冷却，默认 `600`（30 秒）。 |
| `/oxycarbide config rtp autoprotectnum <数量>` | 管理员 | 同时请求数达到该值时触发保护，默认 `3`。 |
| `/oxycarbide rule rtpAutoProtect <true\|false>` | 管理员 | 只放行第一个随机传送请求，默认 `true`；关闭可能造成卡顿。 |
| `/oxycarbide config tpa switch <true\|false>` | 管理员 | TPA 功能开关。 |
| `/oxycarbide config tpa countdown <tick>` | 管理员 | 同意后到实际传送的延迟，默认 `60`。 |
| `/oxycarbide config tpa cooldown <tick>` | 管理员 | 发起请求的冷却，默认 `100`。 |
| `/oxycarbide config tpa autodeny <tick>` | 管理员 | 请求自动拒绝时间，默认 `100`。 |

### 玩家与聊天

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/hat` | 所有人 | 把主手物品戴到头上（一次只移动 1 个）。 |
| `/nickname <昵称>` | 所有人 | 修改自己的聊天昵称。 |
| `/nickfor <玩家> <昵称>` | 管理员 | 修改他人的聊天昵称。 |
| `/mute <玩家>` | 管理员 | 按默认时长禁言。 |
| `/fmute <玩家>` | 管理员 | 永久禁言，只能由 `/unmute` 解除。 |
| `/unmute <玩家>` | 管理员 | 解除禁言。 |
| `/kickme` | 所有人 | 主动断开连接，用于卡死时脱身。 |
| `/invsee <玩家>` | 管理员 | 以箱子界面查看并操作目标玩家的背包。 |
| `/trashcan` | 所有人 | 打开临时垃圾桶，关闭后内容全部删除。 |
| `/oxycarbide config nickname switch <true\|false>` | 管理员 | 昵称功能开关。 |
| `/oxycarbide config mute switch <true\|false>` | 管理员 | 禁言功能开关。 |
| `/oxycarbide config mute timer <tick>` | 管理员 | `/mute` 的默认时长，默认 `1200`（1 分钟）。 |
| `/oxycarbide config kickme switch <true\|false>` | 管理员 | `/kickme` 开关。 |
| `/oxycarbide config trashcan switch <true\|false>` | 管理员 | `/trashcan` 开关。 |

### 登录

开启后，未登录的玩家会被冻结（无法移动、交互、破坏方块或使用物品），超时后自动
踢出服务器。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/login <密码>` | 所有人 | 简单模式：登录，首次使用即注册。 |
| `/login <用户名> <密码>` | 所有人 | 普通模式：登录，首次使用即注册。 |
| `/loginedit <旧用户名> <旧密码> <新用户名> <新密码>` | 管理员 | 普通模式下修改登录信息。 |
| `/loginedit <玩家> <旧密码> <新密码>` | 管理员 | 简单模式下修改登录信息。 |
| `/loginrequest` | 管理员 | 查看本次开服以来的登录请求。 |
| `/loginlist` | 管理员 | 列出全部已注册账号。 |
| `/oxycarbide config login switch <true\|false>` | 管理员 | 登录功能开关，默认关。 |
| `/oxycarbide config login mode <normal\|simple>` | 管理员 | 普通模式（用户名+密码）或简单模式（仅密码）。 |
| `/oxycarbide config login time_first <tick>` | 管理员 | 首次登录超时，默认 `1200`。 |
| `/oxycarbide config login time <tick>` | 管理员 | 之后登录的超时，默认 `600`。 |

### 经济

金币是基础货币，名称可以自定义；特殊货币拥有相对金币的汇率。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/treasury` | 所有人 | 查看自己的金币和全部特殊货币余额。 |
| `/transfer_treasury <玩家> <数量>` | 所有人 | 向其他玩家转账金币。 |
| `/addtreasury <玩家> <数量>` | 管理员 | 给玩家增加金币。 |
| `/cutreasury <玩家> <数量>` | 管理员 | 强制扣除金币，余额可为负。 |
| `/listreasury` | 管理员 | 列出全部账户（含 0 余额）及特殊货币。 |
| `/sptreasury` | 所有人 | 列出全部特殊货币及汇率。 |
| `/sptreasury create <名称> <汇率>` | 管理员 | 创建特殊货币，汇率表示 1 单位等于多少金币。 |
| `/add_sptreasury <货币> <玩家> <数量>` | 管理员 | 发放特殊货币。 |
| `/cut_sptreasury <货币> <玩家> <数量>` | 管理员 | 强制扣除特殊货币，可为负。 |
| `/transfer_sptreasury <货币> <玩家> <数量>` | 所有人 | 转账特殊货币。 |
| `/oxycarbide config treasury switch <true\|false>` | 管理员 | 经济功能开关，默认关。 |
| `/oxycarbide config treasury translimit <数量>` | 管理员 | 单次转账上限，`0` 为不限。 |
| `/oxycarbide config treasury currencyname <名称>` | 管理员 | 基础货币名称，默认 `金币`。 |
| `/oxycarbide rule allowPlayerTransTreasury <true\|false>` | 管理员 | 允许玩家间金币转账，默认 `true`。 |
| `/oxycarbide rule allowSpecialCurrency <true\|false>` | 管理员 | 启用特殊货币，默认 `false`。 |
| `/oxycarbide rule allowPlayerTranSPTreasury <true\|false>` | 管理员 | 允许玩家间特殊货币转账，默认 `true`。 |

### 商店与全球市场

商店是纯 JSON 驱动的箱子界面（购买/出售/跳转/装饰）；全球市场是玩家之间挂单交易
的分页界面。两者的界面布局都在 `config/oxycarbide/` 下编辑，详见
[商店界面 JSON](#商店界面-json)。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/shop` | 所有人 | 打开默认商店。 |
| `/shop <id>` | 所有人 | 打开指定商店。 |
| `/shoplist` | 管理员 | 列出已加载的商店。 |
| `/shopreload` | 管理员 | 重新读取商店文件。 |
| `/shop_create <名称> <id>` | 管理员 | 创建空白商店，`id` 用 `~` 自动生成。 |
| `/shop_settings default <id>` | 管理员 | 设置裸 `/shop` 打开的商店。 |
| `/globalshop` | 所有人 | 打开全球市场。 |
| `/sell <货币> <价格> <分钟>` | 所有人 | 上架主手物品；货币 `normal` 为金币，分钟用 `~` 表示默认时长。 |
| `/pull <id>` | 挂单者 | 撤回自己的挂单。 |
| `/spull <id>` | 管理员 | 强制下架任意挂单。 |
| `/oxycarbide config shop switch <true\|false>` | 管理员 | 商店功能开关，默认关。 |
| `/oxycarbide config shop default_time <分钟>` | 管理员 | `/sell` 中 `~` 对应的时长，默认 `180`。 |
| `/oxycarbide config market switch <true\|false>` | 管理员 | 全球市场开关，默认关。 |

### 区块申领

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/claim` | 视规则 | 申领脚下的区块。 |
| `/chunkbelong` | 所有人 | 查看当前区块的所有者与设置。 |
| `/claim_transfer <玩家>` | 所有者 | 把当前区块转让给其他玩家。 |
| `/disclaim` | 所有者 | 放弃当前区块。 |
| `/sp_disclaim` | 管理员 | 强制取消当前区块的申领。 |
| `/claim_settings whitelist add\|remove <玩家>` | 所有者 | 管理区块白名单。 |
| `/claim_settings grounded <true\|false>` | 所有者 | 禁足模式：把非允许玩家推出区块。 |
| `/claim_settings openToTeam <true\|false>` | 所有者 | 同队玩家自动通行。 |
| `/claim_settings publicopen <true\|false>` | 所有者 | 向所有玩家开放。 |
| `/forceload` | 所有者 | 强制加载当前区块。 |
| `/forceload_cancle` | 所有者 | 取消强制加载。 |
| `/forceload_spcancle` | 管理员 | 强制取消强制加载。 |
| `/oxycarbide config claim switch <true\|false>` | 管理员 | 区块申领开关，默认关。 |
| `/oxycarbide config claim value <货币> <数量>` | 管理员 | 申领价格，`0` 免费，`normal` 为金币。 |
| `/oxycarbide config claim maxnum <数量>` | 管理员 | 每名玩家的申领上限，`0` 为不限，默认 `100`。 |
| `/oxycarbide config claim forceloadMaxnum <数量>` | 管理员 | 强制加载上限，`0` 表示禁止，默认 `20`，不会超过 `maxnum`。 |
| `/oxycarbide rule OnlyOpPlayerClaim <true\|false>` | 管理员 | 只有管理员能申领，默认 `true`。 |

### 清扫

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/sweep` | 管理员 | 立即清扫全服掉落物（`entity` 模式同时清理怪物）。 |
| `/oxycarbide config sweep switch <true\|false>` | 管理员 | 清扫总开关，默认开。 |
| `/oxycarbide config sweep time <分钟>` | 管理员 | 自动清扫间隔，`0` 关闭，默认 `5`。 |
| `/oxycarbide config sweep countdown "<列表>"` | 管理员 | 倒计时提示的秒数，逗号分隔，默认 `120,60,30,15,5,4,3,2,1`。 |
| `/oxycarbide config sweep mode <simple\|entity>` | 管理员 | `simple` 只清掉落物，`entity` 连怪物一起清。 |
| `/oxycarbide config sweep whitelist <id>` | 管理员 | 加入清扫白名单（物品或实体 id）。 |
| `/oxycarbide config sweep delwhitelist <id>` | 管理员 | 移出白名单。 |
| `/oxycarbide config sweep smartsweep <true\|false>` | 管理员 | 开启智能清扫，默认关。 |
| `/oxycarbide config smartsweep threshold <tps> <掉落物数>` | 管理员 | 智能清扫触发阈值，默认 TPS `15`、掉落物 `200`。 |
| `/oxycarbide config smartsweep scancountdown <秒>` | 管理员 | 掉落物重新扫描间隔，默认 `15`。 |
| `/oxycarbide config smartsweep sweepcountdown <秒>` | 管理员 | 两次智能清扫的最小间隔，默认 `10`。 |
| `/oxycarbide rule slienceSweep <true\|false>` | 管理员 | 静默清扫，不发送任何清扫提示，默认 `false`。 |

智能清扫会自行采样服务器 tick 时间估算 TPS；只有在 TPS 低于阈值且掉落物数量达到
阈值时才触发清扫，并且受扫描间隔与触发间隔限制。白名单中的 id 在两种模式下都不会
被清理。

### 显示与配置

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/oxycarbide config promptmode` | 所有人 | 查看自己的提示显示位置。 |
| `/oxycarbide config promptmode dialog` | 所有人 | 提示显示在聊天框（默认）。 |
| `/oxycarbide config promptmode title` | 所有人 | 提示显示在屏幕中央的小标题位置。 |

`promptmode` 按玩家保存（`<世界>/oxycarbide_promptmode.json`），互不影响，重启后
仍然有效。`title` 模式下，发给该玩家的系统消息（含全部 Oxycarbide 提示、指令回执、
进服/死亡提示等）都会改为小标题显示；带可点击按钮的消息（如 TPA 的同意/拒绝）会
失去点击能力，这类场景建议保持 `dialog`。

## 自定义文本教程

所有面向玩家的提示都放在配置目录中，首次启动会自动生成；你只需要修改 JSON 里的
**值**，不要改键名。

### 文件位置

```
config/oxycarbide/lang/zh/<名称>_text.json   # 中文
config/oxycarbide/lang/en/<名称>_text.json   # 英文
```

服务端按 JVM 默认语言选择：`zh` 开头用中文文件，其它用英文文件。两份文件互不影响，
建议同时维护。

| 文件 | 对应内容 |
| --- | --- |
| `home_text.json` | 家园设置/传送/删除等提示 |
| `warp_text.json` | 地标创建/传送/重命名/权限提示 |
| `spawn_text.json` | `/spawn` 提示 |
| `back_text.json` | `/back` 提示 |
| `rtp_text.json` | 随机传送提示 |
| `tpa_text.json` | 传送请求相关提示 |
| `hat_text.json` | `/hat` 提示 |
| `nickname_text.json` | 昵称相关提示 |
| `mute_text.json` | 禁言相关提示 |
| `login_text.json` | 登录/注册提示 |
| `treasury_text.json` | 金币相关提示 |
| `sptreasury_text.json` | 特殊货币转账提示 |
| `claim_text.json` | 区块申领提示 |
| `shop_text.json` | 商店购买/出售提示 |
| `market_text.json` | 全球市场提示 |
| `sweep_text.json` | 清扫提示 |

### 修改步骤

1. 启动一次服务器，生成 `config/oxycarbide/lang/` 目录；
2. 用支持 UTF-8 的编辑器打开对应文件（例如中文的
   `config/oxycarbide/lang/zh/home_text.json`）；
3. 只修改冒号后面的文本，保留键名和 JSON 的引号、逗号；
4. 保存后重启服务器（文本在开服时读取，没有热重载指令）。

### 占位符

文本中的 `%s` 会被替换成运行时参数（玩家名、数量、坐标等），`%%` 表示一个普通的
百分号。**占位符数量和顺序必须与默认值一致**，否则会出现参数错位或多余的空缺：

```json
{
  "set": "已将家「%s」设置在 %s"
}
```

上面这条里第一个 `%s` 是家的名字，第二个 `%s` 是坐标（坐标本身是可点击的，点击会
填入 `/tp` 指令）。你可以改成任意措辞，但两个 `%s` 都要保留。

登录相关文本中的 `<用户名>`、`<密码>` 是普通文字，不是占位符，会原样显示。

### 容错

- 文件缺失：使用内置默认文本；
- 某个键缺失：该条消息回退默认值，其它键不受影响；
- JSON 写坏（多逗号、注释、引号不配对）：该文件读取失败，控制台输出警告并整体使用
  默认文本，服务器仍可正常启动。

## 商店界面 JSON

每个商店一个文件：

```
config/oxycarbide/shop/config/<id>.json
```

文件名（不含 `.json`）就是商店 id。推荐流程：`/shop_create "主商店" ~` 创建空商店 →
编辑文件 → `/shopreload` → `/shop <id>` 打开 → `/shop_settings default <id>` 设为
默认商店。

### 商店结构

| 字段 | 类型 | 默认 | 含义 |
| --- | --- | --- | --- |
| `title` | 字符串 | 文件名 | 界面标题。 |
| `rows` | 整数 | `6` | 箱子行数，1–6。 |
| `items` | 数组 | `[]` | 可点击条目。 |

条目中 `row`、`column` 从 **1** 开始，槽位为 `(row-1)*9 + (column-1)`：

| 字段 | 类型 | 默认 | 含义 |
| --- | --- | --- | --- |
| `row` / `column` | 整数 | `1` | 位置。 |
| `display` | 字符串 | `item` | 展示用物品 id。 |
| `enchanted` | 布尔 | `false` | 是否附魔光效。 |
| `name` | 字符串 | 无 | 显示名称。 |
| `lore` | 字符串数组 | 无 | 每行说明。 |
| `type` | 字符串 | `placeholder` | `buy`、`sell`、`redirect`、`placeholder`。 |
| `item` | 字符串 | 无 | 交易物品 id（buy/sell 必填）。 |
| `amount` | 整数 | `1` | 单次交易数量。 |
| `price` | 长整数 | `0` | 单次交易金额，`0` 免费。 |
| `currency` | 字符串 | `normal` | `normal` 为金币，否则为特殊货币名。 |
| `target` | 字符串 | 无 | `redirect` 跳转的商店 id。 |

未知 `type` 会当作 `placeholder`；`display`/`item` 填错会回退成
`minecraft:stone`，不会弄坏整个界面。只有左键会触发交易，右键、Shift 点击、拖拽都
无效，物品也无法从界面中取出。

### 示例

```json
{
  "title": "主商店",
  "rows": 6,
  "items": [
    {
      "row": 2, "column": 2,
      "display": "minecraft:diamond",
      "enchanted": true,
      "name": "钻石",
      "lore": ["左键购买 1 个"],
      "type": "buy",
      "item": "minecraft:diamond",
      "amount": 1,
      "price": 100
    },
    {
      "row": 2, "column": 4,
      "display": "minecraft:emerald",
      "name": "绿宝石",
      "lore": ["左键出售 1 个"],
      "type": "sell",
      "item": "minecraft:emerald",
      "amount": 1,
      "price": 50
    },
    {
      "row": 2, "column": 8,
      "display": "minecraft:compass",
      "name": "前往下一页",
      "type": "redirect",
      "target": "other_shop_id"
    }
  ]
}
```

## 全球市场布局

`/globalshop` 的分页界面上下两行是装饰区，可在 `config/oxycarbide/globalshop_default.json`
中调整：

```json
{
  "title": "世界市场",
  "rows": 6,
  "previous": { "display": "minecraft:paper", "name": "上一页" },
  "next": { "display": "minecraft:paper", "name": "下一页" },
  "frame": [
    { "row": 1, "column": 1, "display": "minecraft:black_stained_glass_pane", "type": "placeholder" },
    { "row": 6, "column": 2, "display": "minecraft:compass", "name": "返回主商店", "type": "redirect", "target": "shop1" }
  ]
}
```

`frame` 里的条目只支持 `redirect` 和 `placeholder`，其它类型会被强制当作
`placeholder`；商品占据上下装饰行之间的区域。挂单、撤回、过期返还都会保留完整物品
数据，卖家离线时物品会进入待领取队列，下次上线自动发放。

## 数据文件

| 路径 | 内容 |
| --- | --- |
| `config/oxycarbide.json` | 全部功能配置（亦可游戏内修改）。 |
| `config/oxycarbide/lang/zh\|en/*_text.json` | 自定义文本。 |
| `config/oxycarbide/shop/config/<id>.json` | 商店界面。 |
| `config/oxycarbide/globalshop_default.json` | 全球市场装饰布局。 |
| `<世界>/oxycarbide_homes.json` | 家园。 |
| `<世界>/oxycarbide_warps.json` | 地标。 |
| `<世界>/oxycarbide_nicknames.json` | 昵称。 |
| `<世界>/oxycarbide_mutes.json` | 禁言。 |
| `<世界>/oxycarbide_lastplayer.json` | 最近玩家位置（`/telepoint_last`）。 |
| `<世界>/oxycarbide_accounts.json` | 登录账号。 |
| `<世界>/oxycarbide_treasury.json` | 金币账户。 |
| `<世界>/oxycarbide_sptreasury.json` | 特殊货币。 |
| `<世界>/oxycarbide_claims.json` | 区块申领。 |
| `<世界>/oxycarbide_market.json` | 全球市场挂单。 |
| `<世界>/oxycarbide_market_pending.json` | 待领取的市场返还物品。 |
| `<世界>/oxycarbide_promptmode.json` | 每名玩家的提示显示位置。 |

备份时请同时备份配置目录与世界存档中的以上文件。

## 许可证

本项目基于 MIT 许可证，详见 [LICENSE](LICENSE)。它由 Fabric 示例模组改造而来，
后者同样以 MIT 发布。
