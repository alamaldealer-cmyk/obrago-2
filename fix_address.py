with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
seen_address = False
seen_editing = False

in_addresses_screen = False

for line in lines:
    if "fun MyAddressesScreenView(" in line:
        in_addresses_screen = True
        seen_address = False
        seen_editing = False
        
    if "fun " in line and "MyAddressesScreenView" not in line and line.strip().startswith("fun "):
        in_addresses_screen = False
        
    if in_addresses_screen:
        if "var showAddressDialog by remember { mutableStateOf(false) }" in line:
            if seen_address:
                continue
            seen_address = True
        if "var editingItem by remember { mutableStateOf<AddressItem?>(null) }" in line:
            if seen_editing:
                continue
            seen_editing = True

    new_lines.append(line)

with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "w") as f:
    f.writelines(new_lines)
