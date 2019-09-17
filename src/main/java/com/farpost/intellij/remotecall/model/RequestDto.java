package com.farpost.intellij.remotecall.model;

import java.util.Objects;

/**
 *
 */
public class RequestDto {

  public RequestDto() {
  }

  public RequestDto(String target, String oldLocator, String newLocator) {
    this.target = target;
    this.oldLocator = oldLocator;
    this.newLocator = newLocator;
  }

  private String target;
  private String oldLocator;
  private String newLocator;

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getOldLocator() {
    return oldLocator;
  }

  public void setOldLocator(String oldLocator) {
    this.oldLocator = oldLocator;
  }

  public String getNewLocator() {
    return newLocator;
  }

  public void setNewLocator(String newLocator) {
    this.newLocator = newLocator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestDto dto = (RequestDto)o;
    return target.equals(dto.target) && oldLocator.equals(dto.oldLocator) && newLocator.equals(dto.newLocator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, oldLocator, newLocator);
  }

  @Override
  public String toString() {
    return "RequestDto{" + "target='" + target + '\'' + ", oldLocator='" + oldLocator + '\'' + ", newLocator='" + newLocator + '\'' + '}';
  }
}
