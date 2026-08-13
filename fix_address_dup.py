import re

with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
in_my_addresses = False
seen_show = False
seen_editing = False

for line in lines:
    if "fun MyAddressesScreenView" in line:
        in_my_addresses = True
        seen_show = False
        seen_editing = False
    
    if line.strip().startswith("fun ") and "MyAddressesScreenView" not in line:
        in_my_addresses = False
        
    if in_my_addresses:
        if "var showAddressDialog by remember" in line:
            if seen_show:
                continue
            seen_show = True
        if "var editingItem by remember" in line:
            if seen_editing:
                continue
            seen_editing = True
            
    new_lines.append(line)

with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "w") as f:
    f.writelines(new_lines)
