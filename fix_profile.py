import re

with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "IconButton(" in line and "msgText.isNotBlank()" not in line and "chatMessages.add" not in line and "onClick = {" not in line and "Icon(" not in line:
        if i+1 < len(lines) and "IconButton(\\" in lines[i+1]:
            # This is the start of the mangled block!
            skip = True
            
            # Insert the correct block
            new_lines.append("""                        IconButton(
                            onClick = {
                                if (msgText.isNotBlank()) {
                                    chatMessages.add("You: $msgText")
                                    val userQuery = msgText
                                    msgText = ""
                                    // Save to Firestore
                                    user?.let { u ->
                                        val ticket = mapOf(
                                            "userId" to u.id,
                                            "userName" to u.name,
                                            "message" to userQuery,
                                            "timestamp" to System.currentTimeMillis(),
                                            "status" to "open"
                                        )
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("support_tickets").add(ticket)
                                    }
                                    chatMessages.add("Obrago Agent: Thank you for asking about '$userQuery'. A ticket has been sent to Admin. We will resolve this shortly!")
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = ObragoGreenDark)
                        }
                    }
                }
            },
            confirmButton = {
""")
            continue
            
    if skip:
        if "confirmButton = {" in line and "            confirmButton = {" in line:
            skip = False
        continue
        
    new_lines.append(line)

with open("app/src/main/java/com/obrago/app/ui/customer/ProfileScreen.kt", "w") as f:
    f.writelines(new_lines)
