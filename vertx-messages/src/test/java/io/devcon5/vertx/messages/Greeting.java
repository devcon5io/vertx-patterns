package io.devcon5.vertx.messages;

/**
 *
 */
public class Greeting {

  private String greeting;
  private User user;

  public User getUser() {

    return user;
  }

  public void setUser(final User user) {

    this.user = user;
  }

  public String getGreeting() {

    return greeting;
  }

  public void setGreeting(final String greeting) {

    this.greeting = greeting;
  }

  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder(greeting);
    sb.append(' ');
    sb.append(user.getName());
    sb.append('!');
    return sb.toString();
  }
}
