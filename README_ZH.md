# Oxycarbide 一站式服务器管理

![Minecraft](https://img.shields.io/badge/Minecraft-Java%20Edition-brightgreen)
![Fabric](https://img.shields.io/badge/Fabric-26.3%20%7C%201.21.1%20%7C%201.20.1-orange)
![License](https://img.shields.io/badge/License-MIT-blue)
![CurseForge](https://img.shields.io/badge/CurseForge-available%20now-orange)

[English](README.md) | **简体中文**

Oxycarbide 是面向高版本 Minecraft 服务端的服务器管理模组，为玩家和管理员提供了许多实用指令和功能。
多数功能默认开启，登录、经济、区块申领、商店、全球市场默认关闭，需要时用指令打开。
大部分面向玩家的提示都可以通过 JSON 自定义。
现兼容Fabric MC26.3|26.2|26.1.x|1.21|1.20版本。

**提醒：mod暂处于早期beta版本，且包含ai生成内容，请谨慎使用！**

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
| 菜单 GUI | 开 | `/oxycarbide config menu switch true` |
| 侧边栏（记分板） | 关 | `/oxycarbide config scoreboard switch true` |
| 称号 | 开 | `/oxycarbide config titles switch true` |
| 全球市场 | 关 | `/oxycarbide config market switch true` |

关闭某个功能后，它的全部指令都会回复“该功能已关闭”，不会执行任何操作。各模块执行
`/oxycarbide config <模块>`（不带参数）可以查看当前配置。

权限说明：`管理员` 指权限等级 2（OP），`所有人` 指任意玩家，`所有者` 指资源创建者。

所有需要填写已有对象的参数都可以用 `Tab` 补全：`/tpa`、`/mute`、`/addtreasury` 等补全
在线玩家名；`/home`、`/delhome` 补全自己的家名称；`/warp` 补全地标名；`/shop` 补全商店
id；`/menu`、`/menu_default` 补全菜单 id；`/pull` 补全市场挂单 id；`/sell` 补全货币名；
所有 `switch` 与规则补全 `true`/`false`；`delwhitelist` 补全清扫白名单；`/opdelhome`
补全全服的家名称。

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

### 菜单

菜单同样是纯 JSON 驱动的箱子界面：每个按钮要么以点击者的身份执行一条指令，要么
跳转到另一个菜单页面。文件放在 `config/oxycarbide/menu/`，制作方法见
[菜单界面 JSON](#菜单界面-json)。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/menu` | 所有人 | 打开默认菜单。 |
| `/menu <id>` | 所有人 | 打开指定菜单。 |
| `/menu_default <id>` | 管理员 | 设置裸 `/menu` 打开的菜单。 |
| `/menu_create [id]` | 管理员 | 创建一个空白菜单；`id` 填 `~` 或不填时随机生成。 |
| `/menu_list` | 管理员 | 列出已加载的菜单。 |
| `/menu_reload` | 管理员 | 重新读取菜单文件。 |
| `/oxycarbide config menu switch <true\|false>` | 管理员 | 菜单功能开关，默认开。 |

文件名就是菜单 id；首次启动会自动生成 `example.json`，可以直接复制它来改。按钮以
**点击者本人**的权限执行，所以菜单不会让普通玩家获得额外权限。关闭菜单功能后，`/menu`
会回复"该功能已关闭"，但文件管理指令（`/menu_create`、`/menu_default`、`/menu_list`、
`/menu_reload`）仍然可用。

### 侧边栏

侧边栏是按玩家独立显示的记分板，内容由 `config/oxycarbide/scoreboard/scoreboard.json`
描述，制作方法见 [侧边栏 JSON](#侧边栏-json)。默认关闭，且完全不会创建或修改原版
`/scoreboard` 的记分项。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/oxycarbide scoreboard` | 所有人 | 查看侧边栏是否开启、行数以及刷新间隔。 |
| `/oxycarbide scoreboard reload` | 管理员 | 重新读取 `scoreboard.json`。 |
| `/oxycarbide scoreboard counter set <计数器> <玩家> <数值>` | 管理员 | 设置 `custom` 行使用的自定义计数器。 |
| `/oxycarbide scoreboard counter add <计数器> <玩家> <数值>` | 管理员 | 在自定义计数器上加减（负数即减少）。 |
| `/oxycarbide config scoreboard switch <true\|false>` | 管理员 | 侧边栏功能开关，默认关。 |
| `/oxycarbide rule DisplayOfflinePlayerScoreboard <true\|false>` | 管理员 | 侧边栏榜单是否也显示离线玩家，默认 `false`。 |

### 称号

称号是显示在玩家名字前面的彩色前缀，聊天栏和头顶名字都会显示。称号同样是纯 JSON
（`config/oxycarbide/title/titles.json`，见 [称号 JSON](#称号-json)），完全**不使用**
原版 team 功能：聊天前缀来自显示名，头顶名字用的是实体的自定义名称。

指令放在 `/titles` 而不是 `/title`：原版的 `/title` 是屏幕标题指令，占用同名根节点要么会
把原版指令顶掉，要么会继承它"仅管理员"的要求（那样 `/titles wear` 普通玩家就用不了）。

| 指令 | 权限 | 说明 |
| --- | --- | --- |
| `/titles` | 所有人 | 查看自己当前佩戴的称号。 |
| `/titles create <文本> <颜色> <id>` | 管理员 | 创建称号；`id` 填 `~` 自动生成，`文本` 含空格时用引号。 |
| `/titles get <id> [玩家]` | 管理员 | 把称号发给自己，或发给指定玩家。 |
| `/titles wear <id>` | 所有人 | 佩戴自己已获得的称号，只补全已获得的 id。 |
| `/titles clear` | 所有人 | 取消佩戴称号。 |
| `/oxycarbide config titles switch <true\|false>` | 管理员 | 称号功能开关，默认开。 |

商店也可以卖称号：用 `command` 类型的商品、指令写 `/titles get vip %player%`，购买后就会
以服务端权限替买家执行这条管理员指令，详见 [商店界面 JSON](#商店界面-json)。

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
| `/oxycarbide config promptmode actionbar` | 所有人 | 提示以物品栏上方的小字显示。 |
| `/oxycarbide reload` | 所有人 | 查看两种重载方式的作用。 |
| `/oxycarbide reload server` | 管理员 | 重新读取 `config/oxycarbide.json`、所有语言文件、商店与菜单文件。 |
| `/oxycarbide reload client` | 所有人 | 让自己的游戏重载本地资源（等同 `F3 + T`）。 |

`promptmode` 按玩家保存（`<世界>/oxycarbide_promptmode.json`），互不影响，重启后
仍然有效。`actionbar` 模式下，发给该玩家的系统消息（含全部 Oxycarbide 提示、指令回执、
进服/死亡提示等）都会改为在物品栏上方以小字显示，几秒后自动淡出。唯一的例外是传送请求：
`/tpa` 与 `/tpahere` 始终发到聊天框，因为绿色的**同意**与红色的**拒绝**按钮是可点击文本，
而物品栏上方的小字无法点击。旧的 `title` 值（屏幕中央小标题）仍可作为别名使用，并在读取
存档时自动转换为 `actionbar`。

`reload server` 不需要重启服务器，改完文本或配置后用它即可立即生效；它会重载设置文件、
每种语言的 19 个文本文件、商店与菜单文件以及每位玩家的提示显示模式。`reload client`
只影响执行者自己的游戏，并且要求该客户端也安装了 oxycarbide（仅装在服务端时会提示客户端
没有可重载的内容）。

## 自定义文本教程

所有面向玩家的提示都放在配置目录中，首次启动会自动生成；你只需要修改 JSON 里的
**值**，不要改键名。

## 数据文件

| 路径 | 内容 |
| --- | --- |
| `config/oxycarbide.json` | 全部功能配置（亦可游戏内修改）。 |
| `config/oxycarbide/lang/<语言>/*_text.json` | 自定义文本。 |
| `config/oxycarbide/shop/config/<id>.json` | 商店界面。 |
| `config/oxycarbide/menu/<id>.json` | 菜单界面。 |
| `config/oxycarbide/scoreboard/scoreboard.json` | 侧边栏布局。 |
| `config/oxycarbide/title/titles.json` | 称号定义。 |
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
| `<世界>/oxycarbide_counters.json` | 侧边栏的自定义计数器。 |
| `<世界>/oxycarbide_titles.json` | 已获得与正在佩戴的称号。 |
| `<世界>/oxycarbide_market.json` | 全球市场挂单。 |
| `<世界>/oxycarbide_market_pending.json` | 待领取的市场返还物品。 |
| `<世界>/oxycarbide_promptmode.json` | 每名玩家的提示显示位置。 |

备份时请同时备份配置目录与世界存档中的以上文件。

## 许可证

本项目基于 MIT 许可证，详见 [LICENSE](LICENSE)。它由 Fabric 示例模组改造而来，
后者同样以 MIT 发布。
