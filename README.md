# ExpandedMenuView
Expanded Menu View library

For one application, I had to develop such a menu, so I decided to share it with everyone

![simple](images/expandedmenuview.gif)

### Usage

Reference the library from your module's build.gradle:

``` gradle
dependencies {
    [...]
    implementation 'com.github.luksoral:ExpandedMenuView:[latest_version]'
}
```

Latest version: [![](https://jitpack.io/v/luksoral/ExpandedMenuView.svg)](https://jitpack.io/#luksoral/ExpandedMenuView)

Add `ExpandedMenuView` as a view to the layout:

``` xml
<pro.midev.expandedmenulibrary.ExpandedMenuView
     android:id="@+id/expMenu"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     app:em_background_color="@color/colorPrimary"
     app:em_shadow_color="@color/colorPrimaryDark"
     app:em_menu_icon="@drawable/ic_menu"
     app:em_close_menu_icon="@drawable/ic_close_menu"
     app:em_text_color="@android:color/white"/>
```

`em_background_color` - menu background color\
`em_menu_icon` - menu icon drawable\
`em_close_menu_icon` - close menu icon drawable\
`em_shadow_color` - menu shadow color\
`em_text_color` - menu title color

Add code below for icons and titles initialization (can take 3 or 4 items)

``` xml
<expandableMenuViewId>.setIcons(
            ExpandedMenuItem(R.drawable.ic_home, "Home"),
            ExpandedMenuItem(R.drawable.ic_qr, "QR-code"),
            ExpandedMenuItem(R.drawable.ic_main_menu, "Menu"),
            ExpandedMenuItem(R.drawable.ic_profile, "Profile")
        )
```

`ExpandableMenuItem` - data object menu item

`ExpandedMenuClickListener` - interface for listen items click
