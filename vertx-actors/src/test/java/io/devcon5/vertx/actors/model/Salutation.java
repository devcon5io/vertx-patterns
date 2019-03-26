package io.devcon5.vertx.actors.model;

import java.util.Objects;

/**
 *
 */
public class Salutation {

  private String greeting;
  private User user;

  public User getUser() {

    return user;
  }

  public void setUser(final User user) {

    this.user = user;
  }

  public Salutation withUser(final User user) {
    setUser(user);
    return this;
  }

  public String getGreeting() {

    return greeting;
  }

  public void setGreeting(final String greeting) {

    this.greeting = greeting;
  }

  public Salutation withGreeting(final String greeting) {
    setGreeting(greeting);
    return this;
  }

  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder(greeting);
    sb.append(' ');
    sb.append(user.getName());
    sb.append('!');
    return sb.toString();
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Salutation that = (Salutation) o;
    return Objects.equals(greeting, that.greeting) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {

    return Objects.hash(greeting, user);
  }
}
