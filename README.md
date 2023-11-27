# README file for Chatter app
## how to start?

### Download our server file
- `cd ./server`
- `npm install`
- `node .`

### Download our Chatter file and open it in Android Studio

Congrats! You are in our application!


Welcome to our chatting app called Chatter.


### How to use our app:
First of all, our app will open in the login page.
There, you will have a settings button of which you should enter the url of the server.

For example, our server was running on http://192.168.1.46:5000/. Make sure to add the '/' at the end.

In the settings screen you can also turn the app to be in a dark mode, if you prefer.

From the login page, you can try to type in a username and password but you won't be able to log in since you don't have an account yet (You will get the "Username doesn't exist" message).
Therefor, you should start by clicking the "Don't have an account? Click here to register" to create an account.

Now you are in our registeration page. Go ahead and create an account with your desired username, password, display name and profile picture.

Your password must be at least 6 characters, and your password verification must match the password. Otherwise you won't be able to register, and get a matching
error message. Also, you must fill in all of the fields. Trying to register with a username that already exists also won't work (you'll get an error message).

If you entered everything properly and register, you will reach the log in screen. Enter your username and password and arrive at our Chatter Chat Screen!

Right now the chat screen is blank because you just created an account. To add chats, click on the Add Contact button (on the bottom right, a contact logo on it).

Now you can add whichever contact you want, assuming they exist (registered to our application).

When you are done adding your contacts, go back to the chat screen with the back arrow on the top left. 


Now you have a list of all the chats you created. Entering a chat will take you to the chat screen in which you can send messages to your friend. You can write a message in the input field, and press the green Send button to send the message.
Each contact will have it's own chat with the messages you sent them.

If you are done chatting, you can click the red log out button to return to the login page.

From there you can either log back in to your account with your username and password, or you can register to another account.
