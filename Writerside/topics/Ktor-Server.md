# Ktor Server

Kobold uses [Kotlin Symbol Processing (KSP)](https://kotlinlang.org/docs/ksp-overview.html) to build upon [Ktor Type Safe Routing](https://ktor.io/docs/server-resources.html).

Kobold will maintain your ktor server routing based on class structures so all you need in your ktor server setup is:

```kotlin
 embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        autoRoute()
    }.start(wait = true)
```

Kobold will detect any class annotated with `@Resource` and `@Kobold` and add it to the autoRouter() function: 

```Kotlin

@Kobold @Resource("/login")
data object LoginPage: KPost<MyLoginData, MyLoginResponse>
{
 
	override val process: (MyLoginData) -> MyLoginResponse = {
		MyLoginResponse("I got your Post, here is the response!")
	}
}
```

By extending `KPost<T, R>` you direct Kobold to create a `POST` route for `/login` which will accept 
`T` as the post body and respond with `R`.  `T` implements `KRequest` and `R` implement `KResponse`.

`AuthRouter` will capture the request and processes it via a lambda to your personal `process` implementation.

The resulting route will look like this:

```Kotlin
fun Application.autoRouter() {
  routing {
    post<LoginPage> {
      val response : MyLoginResponse = it.process(call.receive())
      call.respond(response,  typeInfo = TypeInfo(MyLoginResponse::class))
    }
  }
}
```

Routing can get very complex as your app grows and Kobold will keep it clean for you. Learn more about using Sealed Subclasses to maintain hierarchy.