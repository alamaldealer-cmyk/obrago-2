with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "r") as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    for char in line:
        if char == '{':
            count += 1
        elif char == '}':
            count -= 1
    if count < 0:
        print(f"Negative count at line {i+1}: {line}")
        break

if count != 0:
    print(f"Final count: {count}")
    
