package io.devcon5.vertx.actors.model;

import java.util.Objects;

/**
 *
 */
public class User {

  private String name;

  public User() {}

  public User(final String name) {
    this.name = name;

  }

  public String getName() {

    return name;
  }

  public void setName(final String name) {

    this.name = name;
  }

  public User withName(final String bob) {
    setName(bob);
    return this;
  }

  @Override
  public boolean equals(final Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final User user = (User) o;
    return Objects.equals(name, user.name);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name);
  }
}
