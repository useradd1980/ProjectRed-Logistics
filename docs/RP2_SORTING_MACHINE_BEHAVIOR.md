# RedPower 2 pr6 Sorting Machine behaviour notes

These notes were recovered from the user's original
`RedPowerMechanical-2.0pr6.zip`, principally `TileSorter.class` and
`MachineLib.class`. They are implementation references for compatibility,
not redistributed RedPower source.

## Configuration layout

- 40 configuration stacks arranged as 5 rows x 8 columns.
- One routing colour per column.
- Modes 4 and 6 additionally use a default routing colour.

## Sorting modes

0. **Anystack Sequential**
   - Uses the current non-empty column.
   - Requires one configured stack in that column to be satisfiable.
   - Extracts that configured quantity.
   - Advances to the next non-empty column.

1. **Allstack Sequential**
   - Uses the current non-empty column.
   - Requires every configured stack in that column.
   - Extracts every configured stack quantity.
   - Advances to the next non-empty column.

2. **Random Allstack**
   - Scans columns 0..7 and chooses the first complete column.
   - Extracts all configured stacks in that column.

3. **Any Item**
   - Finds any satisfiable configured stack anywhere in the 5x8 grid.
   - Extracts the configured quantity.

4. **Any Item + Default Route**
   - Same configured-match behaviour as mode 3.
   - If no configured stack is satisfiable, extracts the first available
     source stack and assigns the default colour.

5. **Any Item Whole Stack**
   - Match selection still requires at least the configured quantity.
   - Once matched, extraction collects up to the item's maximum stack size.

6. **Whole Stack + Default Route**
   - Mode 5 for configured matches.
   - Mode 4-style fallback for unmatched/default-routed items.

## Incoming tube payloads

- Empty filter: accepts any normal-priority payload and outputs it uncoloured.
- Non-empty filter: matched payloads are assigned the matching column colour.
- Unmatched payloads are accepted only in modes 4 and 6, using the default
  colour.
- In pr6, power/buffer availability also gates acceptance.

## Pull modes

- Pull mode 0 is redstone-triggered single operation.
- Pull mode 1 automatically retries extraction on scheduled ticks.
- Pull mode 2 counts redstone pulses.
  - In sequential modes (0/1), one pulse is consumed when the column cycle
    wraps, producing a sweep through configured columns.
  - In modes 2..6, one successful extraction consumes one pulse.


## Power behaviour

Recovered from the original pr6 `TileSorter.class`:

- The sorter implements the old BluePower/Blutricity connection interface.
- Normal incoming tube payloads are rejected below 60 V.
- Adjacent-inventory extraction also refuses to run below 60 V.
- Successful sorting consumes `25 * itemCount` power units for each stack
  processed.
- Backstuff entering from the output side is accepted without the 60 V check.
- ProjectRed Logistics maps this to ProjectRed's modern low-load Electrotine
  network. Its conductor charge scale uses 600 == 60 V, so the original
  threshold maps directly.
