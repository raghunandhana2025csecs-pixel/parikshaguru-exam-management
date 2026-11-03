package com.parikshaguru.config;
import java.util.*;
public class AppProperties {
  public static class Jwt { public String getSecret(){return "";} public long getExpirationMinutes(){return 1;} }
  public static class Auth { public List<String> getAllowedEmailDomains(){return List.of();} public String getFacultyInviteCode(){return "";}
    public boolean isStudentAutoActivate(){return true;} public boolean isExposeResetToken(){return false;} public int getResetTokenMinutes(){return 30;} }
  public static class Seed { public boolean isEnabled(){return true;} public boolean isDemoData(){return true;} public String getAdminName(){return "";}
    public String getAdminEmail(){return "";} public String getAdminPassword(){return "";} }
  public Jwt getJwt(){return null;} public Auth getAuth(){return null;} public Seed getSeed(){return null;}
}
