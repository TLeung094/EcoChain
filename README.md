# EcoChain

EcoChain 是一款為 Paper 及 Folia 伺服器而設的 Minecraft 生態系統插件。它會以區塊（Chunk）為單位記錄玩家對環境的影響，並把生態狀況分為綠化度、動物親和度及水源純淨度三項指標。玩家的種植、伐木、繁殖、捕獵及水源操作都會改變當地生態，生態狀況亦會反過來影響作物、動物及釣魚體驗。

## 功能特色

- **區域生態評分**：每個區塊獨立保存 `-100` 至 `100` 的綠化、動物及水質數值。
- **動態生態階段**：按總體評分顯示豐饒淨土、和諧林地、平凡田野、荒蕪之地或死寂廢土。
- **玩家行為連動**：種植植物、繁殖動物及放置水源可改善生態；伐木、殺死動物及放置岩漿則會令生態惡化。
- **環境回饋**：良好生態可加快作物或樹苗生長、提高收成、縮短動物繁殖冷卻，並提升釣到寶物的機會；污染水域則可能只釣到垃圾。
- **生態衰退與懲罰**：區域數值會隨時間逐步回歸中性；總體評分過低時，當地玩家會受到飢餓及挖掘疲勞效果。
- **每日生態任務**：玩家每日登入會獲得任務，完成後可取得經驗值獎勵。
- **生態點數商店**：使用經驗值兌換稀有樹苗及動物生怪蛋。
- **生態探測羅盤**：透過 GUI 查看當前區塊的各項指標及總體生態階段。
- **資料持久化**：區域資料儲存於本機 SQLite，玩家任務資料則以 YAML 保存。
- **PlaceholderAPI 支援**：可把即時生態數據顯示於計分板、TAB 或其他相容插件。
- **Folia 支援**：採用區域及非同步排程 API，支援 Folia 架構。

## 系統需求

- Java 21 或以上
- Paper / Folia `26.2` 或以上版本
- PlaceholderAPI `2.11.6` 或以上（選用）

> [!NOTE]
> `plugin.yml` 使用 `api-version: '26.2'`。請確保伺服器版本與插件所使用的 Paper API 相容。

## 安裝方法

1. 從 Releases 下載最新的 EcoChain JAR，或依照下方步驟自行建置。
2. 把 JAR 放入伺服器的 `plugins/` 資料夾。
3. 如需使用生態 Placeholder，請同時安裝 PlaceholderAPI。
4. 啟動或重新啟動伺服器。
5. 插件會自動建立設定檔、SQLite 資料庫及玩家任務資料夾。

## 指令

| 指令 | 說明 | 權限 |
| --- | --- | --- |
| `/ecochain` | 查看玩家所在區塊的生態報告 | 無 |
| `/ecoquest` | 查看今日生態任務及進度 | 無 |
| `/ecoshop` | 開啟生態點數商店 | 無 |
| `/ecoadmin set <flora\|fauna\|aqua> <數值>` | 設定當前區塊的指定生態值，數值會限制於 `-100` 至 `100` | `ecochain.admin` |
| `/ecoadmin reset` | 把當前區塊的所有生態值重設為 `0` | `ecochain.admin` |
| `/ecoadmin compass` | 取得生態探測羅盤 | `ecochain.admin` |
| `/ecoadmin reload` | 重新載入 `config.yml` | `ecochain.admin` |

`ecochain.admin` 預設只開放予伺服器管理員（OP）。所有指令目前均須由遊戲內玩家執行。

## 生態指標

| 指標 | 改善方式 | 惡化方式 |
| --- | --- | --- |
| 綠化度（Flora） | 種植樹苗、花卉、農作物、蘑菇、竹、仙人掌等植物 | 砍伐原木或木材 |
| 動物親和度（Fauna） | 繁殖動物 | 玩家殺死動物 |
| 水源純淨度（Aqua） | 放置水或部分水生生物桶 | 抽走水源或放置岩漿 |

總體生態評分是三項指標的平均值：

| 總體評分 | 生態階段 |
| ---: | --- |
| `80` 至 `100` | 豐饒淨土 |
| `50` 至 `79` | 和諧林地 |
| `0` 至 `49` | 平凡田野 |
| `-30` 至 `-1` | 荒蕪之地 |
| `-100` 至 `-31` | 死寂廢土 |

## PlaceholderAPI

安裝 PlaceholderAPI 後，可使用以下變數顯示玩家當前區塊的生態資料：

| Placeholder | 回傳內容 |
| --- | --- |
| `%ecochain_flora%` | 綠化度 |
| `%ecochain_fauna%` | 動物親和度 |
| `%ecochain_aqua%` | 水源純淨度 |
| `%ecochain_overall%` | 總體生態評分 |
| `%ecochain_stage%` | 生態階段名稱 |

## 設定

主要設定位於 `plugins/EcoChain/config.yml`：

```yaml
decay:
  interval-ticks: 12000
  amount: 2

weights:
  flora:
    plant-sapling: 2
    break-log: -1
  fauna:
    breed-animal: 3
    kill-animal: -2
  aqua:
    place-water: 2
    pickup-water: -1
    place-lava: -5

quest:
  exp-reward: 50
```

- `decay.interval-ticks`：生態衰退檢查週期，`20 ticks = 1 秒`。
- `decay.amount`：每次衰退向 `0` 回復的數值。
- `weights.*`：各種玩家行為對生態指標的影響。
- `quest.exp-reward`：完成每日任務所得的經驗值。

修改設定後，可使用 `/ecoadmin reload` 重新載入。

## 從原始碼建置

```bash
git clone https://github.com/TLeung094/EcoChain.git
cd EcoChain
mvn clean package
```

完成後，可在 `target/` 目錄找到建置好的 JAR。

## 技術棧

- Java 21
- Paper / Folia API
- Maven
- SQLite + HikariCP
- PlaceholderAPI（選用）

## 授權

此 repository 目前未有授權檔案。在加入正式授權前，所有權利由原作者保留。