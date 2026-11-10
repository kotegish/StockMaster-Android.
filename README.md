# SoloDefStudio StockMaster — prototype 0.1

Android prototype: local SQLite catalog, dashboard, product search, stock receipt/expense, movement history.

## Build
GitHub Actions workflow **Build StockMaster APK** runs on every push to `main` and can also be started manually. It creates a debug APK for device testing, not a signed commercial release.

## Prototype limitations
- Barcode field is manual; camera scanning is the next implementation step.
- CSV/Excel import/export and backup UI are not included yet.
- Demo products are inserted on first launch.
- Stock operations are recorded in a movement ledger and update local balances.
