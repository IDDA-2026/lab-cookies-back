# Spring Boot: Cookies example

<img alt="a guy hugging the java logo" src="./public/main.png" />

## What this project is

This is the **starting example** for the lesson. It is a working Spring Boot backend that already has signup, login, a protected route, and logout.

Right now the token travels inside the **JSON body**, and the security filter reads it from the **Authorization header**. This project does **not** use cookies yet.

Your job in this lesson is to add the cookie code yourself, so that:

1. The backend **sets** the token as an httpOnly cookie when you log in (the cookie setter).
2. The backend **reads** the token back from that cookie on every request (the cookie getter).

## What you need to build

Follow the lesson in the portal step by step. You will only work on the **backend** here. You will touch three places:

1. The **login** endpoint, so it sends the token inside an httpOnly cookie instead of the body.
2. The **security filter**, so it reads the token from the cookie instead of the Authorization header.
3. The **logout** endpoint, so it clears the cookie.

Until you add that code, the cookie flow will not work. That is expected. The portal walks you through each piece.

## A demo user is already created

When the app starts, it creates one user for you so you can log in right away:

* email: demo@ironhack.com
* password: password

## How to run the backend

1. Open the project in your IDE.
2. Run the `SimpleAuthApplication` class (the green run button on the main method).
3. The server starts on http://localhost:8080

## The endpoints

* POST http://localhost:8080/api/signup  creates a new user
* POST http://localhost:8080/api/login   logs in and gives you the token
* GET  http://localhost:8080/api/me       protected route, returns the logged in user
* POST http://localhost:8080/api/logout   logs out

## Testing with Postman

You will test only the backend with Postman.

### 1. Log in

1. Create a new request: POST http://localhost:8080/api/login
2. Go to the **Body** tab, choose **raw**, and pick **JSON**.
3. Paste this body:

```json
{
  "email": "demo@ironhack.com",
  "password": "password"
}
```

4. Press **Send**. You should get a 200 response.

### 2. See the cookie that came back

Once you have added the cookie code in the lesson, login will send the cookie back to Postman.

1. Look under the **Send** button for a link called **Cookies**. Click it.
2. You will see a cookie named **token** stored for the domain **localhost**.

Postman keeps these cookies in a small box called the cookie jar. It will send them for you automatically on your next request to localhost, just like a real browser does.

### 3. Call the protected route

1. Create a new request: GET http://localhost:8080/api/me
2. Press **Send**. You do **not** need to add any header by hand.
3. Because Postman already has the token cookie, it sends it for you, and you get back the logged in user.

If you get a 403 here, it means the cookie was not sent or not read. Check that you logged in first and that the token cookie is in the Cookies box.

### 4. Log out

1. Create a new request: POST http://localhost:8080/api/logout
2. Press **Send**. The token cookie is cleared, and calling /api/me again should fail.

### Adding a cookie by hand in Postman (only if you need to)

Most of the time Postman stores the cookie for you. If you ever want to set it yourself:

1. Click the **Cookies** link under the **Send** button.
2. Type the domain **localhost** and click **Add Cookie**.
3. Paste a line like this and save:

```
token=PASTE_YOUR_TOKEN_HERE; Path=/
```

4. Postman will now send that cookie on every request to localhost.

### One thing to watch

If you mark the cookie as Secure, Postman may refuse to send it over plain http on localhost. While you test on http, keep the Secure flag off, or switch to https. If /api/me cannot see your cookie, this is the first thing to check.
