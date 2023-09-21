package com.example.bankSys.classes;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
       
	@Autowired
	private DataSource  datasrc;
	
	@GetMapping("/")
	public String showLandingPage()
	{
		return "index";
	}
	
	@GetMapping("/signin")
	public String showLoginpage(Model model)
	{
		return "signin";
	}
	@GetMapping("/signup")
	public String showsignup(Model model)
	{
		return "signup";
	}
	
	@GetMapping("/mainmenu")
	public String showmainmenu(Model model)
	{
		return "mainmenu";
	}
	@GetMapping("/cancle")
	public String cancle()
	{
		return "redirect:/"; 
	}
	
	@PostMapping("/signin")
	public String userAuthentication(@RequestParam("UserName") String UserName,
			                          @RequestParam("Password")String Password,
			                          Model model,RedirectAttributes rd)
	{
		 boolean Isauthenticate= authenticateUser(UserName,Password);
		if(Isauthenticate)
		{	rd.addFlashAttribute("success", "Welcome To Online Banking");
		  return "redirect:/mainmenu";
		}
		else
		{	rd.addFlashAttribute("error" , "Invalid UserId or Password ");
		    return "redirect:/signin";
		}
	}

	private boolean authenticateUser(String userName, String password) {
		// TODO Auto-generated method stub
		try( Connection con = datasrc.getConnection()){
		 String query = "SELECT * FROM user WHERE username = ? AND password = ?";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, userName);
		ps.setString(2, password);
		ResultSet rs = ps.executeQuery();
		boolean Isauthenticate = rs.next();
		
		return Isauthenticate;
		
		}	
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	@PostMapping("/signup")
	public String usersignup(@RequestParam("newUserName") String newUserName,
            @RequestParam("Password")String newPassword,
            Model model,RedirectAttributes rd)
	{
	try {		 
		  if(usernametaken(newUserName))
		  {	  rd.addFlashAttribute("errorMessage","userid or email is already taken");
	          return "redirect:/signup";  
			  
//			  Connection con = datasrc.getConnection();
//					 String query = "INSERT INTO user (username,password) VALUES(?,?)";		
//		      PreparedStatement ps = con.prepareStatement(query);
//					ps.setString(1, newUserName);
//					ps.setString(2, newPassword);
//					 ps.executeUpdate();
//					 
//					 model.addAttribute("message","Account created successfully");
//					 return "redirect:/mainmenu";
	      }
		else 
		{	 Connection con = datasrc.getConnection();
		    String query = "INSERT INTO user (username,password) VALUES(?,?)";		
	        PreparedStatement ps = con.prepareStatement(query);
				ps.setString(1, newUserName);
				ps.setString(2, newPassword);
				 ps.executeUpdate();
				 
				 rd.addFlashAttribute("success","Account created successfully");
				 return "redirect:/mainmenu";
		}  
	}
	catch(SQLException e)
	{
		e.printStackTrace();
		rd.addFlashAttribute("errorMessage","Account creation is failed,please try again later ");
		return "redirect:/signup";
			
		 
	}
	}
	private boolean usernametaken(String newUsername) throws SQLException {
		// TODO Auto-generated method stub
		Connection con = datasrc.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM user WHERE username=? ");
         ps.setString(1, newUsername);
         ResultSet rs=ps.executeQuery();
                   if(rs.next())
                   {
                     return true;

                  }
                  else
                  {
                    return false;
                  }
		
	}
}
