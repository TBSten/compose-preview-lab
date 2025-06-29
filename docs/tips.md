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
+  onEvent: (HomeUiEvent) -> Unit,
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
     // ⭐️ Even if the number of events increases, there is no need to increase the number of callbacks here.
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

## 4. If you have defined your own app theme, prepare a color model that includes labels and custom modifiers.

If you have defined your own Theme (not MaterialTheme) in your app, include the `label`
property in each color, textStyle, etc. within that Theme.

```kt
data class MyTheme(
    // ⭐️Use MyThemeColor with label set instead of color.
    val primaryColor: MyThemeColor,
    // ...
)

data class MyThemeColor(
    // ⭐️ Instead of simply using Color, prepare MyThemeColor with the label property added.
    val label: String,
    val color: Color,
)

val lightMyTheme = MyTheme(
    primary = MyThemeColor("primary", 0xFFFF0000),
    // ...
)
```

Next, add the following Modifier to the project.
This Modifier not only sets the background, but also applies `Modifier.labAnnotation()` to add annotations that display the
specified color information.

```kt
fun Modifier.background(color: MyThemeColor, shape: Shape): Modifier =
    then(
        Modifier
            .background(color = color.color, shape = shape)
            .labAnnotation(
                label = color.label,
                description = "background(color = ${color.color}, shape = $shape)",
            )
    )
```

Finally, call these as follows.

```kt
fun MyComponent() {
    Box(
        Modifier
            // ⭐️ Use a custom modifier instead of the standard background modifier.
            .background(MyTheme.primaryColor)
    )
}
```

By managing modifiers in this way, the information for the specified values in PreviewLab will be displayed using
the [annotation feature](https://example.com),
eliminating the need to display the code one by one.
