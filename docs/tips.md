> [!WARNING]
> 🚨 This documentation is WIP.

# Tips for Compose Preview Lab

## 1. Putting together events into a UiEvent sealed interface

No need to add more lambdas that call onEvent() as the number of events grows.

```diff
 fun HomeScreen(
   uiState: HomeUiState,
-  onButtonClick: () -> Unit,
-  onNameInput: (String) -> Unit,
   onEvent: (HomeUiEvent) -> Unit,
 ) {
     ...
 }

+sealed interface HomeUiEvent {
+  object ButtonClick : HomeUiEvent
+  data class NameInput(val name: String) : HomeUiEvent
+}

 @Preview
 @Composable
 fun HomeScreenPreview() = PreviewLab {
   HomeScreen(
     ...
     // ⭐️ More events will not require more here.
     onEvent = { onEvent("$it") },
   )
 }
```

## 2. The displayName of @ComposePreviewLabOption can be a placeholder

### Examples

| `displayName` of @ComposePreviewLabOption | Representation example     |
|-------------------------------------------|----------------------------|
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 

## 3. The displayName of @ComposePreviewLabOption can be grouped by `. ` for grouping
