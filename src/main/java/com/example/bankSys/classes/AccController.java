package com.example.bankSys.classes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
//import jakarta.transaction.Transaction;

@Controller

public class AccController {
  
	
	
	@Autowired
	private DataSource datasrc;
	
	private String accNo;
	
	
	@GetMapping("/create-bank-account")
	public String showCreatAccountForm() {
		return "create-account-form";
		
	}
	
	@GetMapping("/view-account-details")
	public String showViewAccountForm() {
		return "view-account-form";
	}
	@GetMapping("/update-account-details")
	public String showUpdateAccountForm() {
		return "update-account-form";
	}
	
	@GetMapping("/validate-account-details")
	public String validateAccount() {
		return "update-account-form";
	}
	
	@GetMapping("/validate-account-number")
	public String validateAccountnum() {
		return "Transfer-form";
	}
	@GetMapping("/transfer-money")
	public String  transferMoney() {
		return "MoneyTransferForm";
	}
	@GetMapping("/validate-account-no")
	public String validateAccountnum2() {
		return "transfer-history-form";
	}
	
	@GetMapping("/transactions")
	public String transactionhistory() {
		return "transaction-table";
	}
	@GetMapping("/validate-account-num")
	public String validateAccountnum3() {
		return "interest-calculation-form";
	}
	@GetMapping("/calculate-interest")
	public String interest() {
		return "interestResult";
	}
	@GetMapping("/validate-account-No")
	public String validateAccountnum1() {
		return "reports-generation-form";
	}
	@GetMapping("/generate-reports") 
	public String transactionreports() {
		return "reportContainer";
	}
	@PostMapping("/create-bank-account")
	public ResponseEntity<Map<String, Object>> createBankAccount(@RequestParam("name") String accHolder,
                                                                 @RequestParam("accType") String accType,
                                                                 @RequestParam("aadharNo") double aadharNo,
                                                                 @RequestParam("dob") Date dob,
                                                                 @RequestParam("initialDeposit") double balance,
                                                                  Model model,RedirectAttributes rd){ 
	    accNo = generateAccnum();
	   
	    Map<String,Object> response = new HashMap<>();
	    
	
	try( Connection con = datasrc.getConnection()){
	String query = "INSERT INTO bank_acc(acc_no,acc_holder,acc_type,aadhar_no,DOB,balance) VALUES (?,?,?,?,?,?)";
	PreparedStatement ps = con.prepareStatement(query);
	ps.setString(1, accNo);
	ps.setString(2, accHolder);
	ps.setString(3, accType);
	ps.setDouble(4, aadharNo);
	ps.setDate(5, new java.sql.Date(dob.getTime()));
	ps.setDouble(6, balance);
	ps.executeUpdate();
	
	response.put("success", true);
	response.put("message", "Account created successfully");
	}
	
	catch (Exception e) {
		e.printStackTrace();
		response.put("success", false);
		response.put("errorMessage", "Account creation failed "+e.getMessage());
	}	
	return ResponseEntity.ok(response);
	
	}
	
	private String generateAccnum() {
		int random = (int)(Math.random()* 1000000)+100000;
		return String.valueOf(random);
	}
	
	
	@PostMapping("/view-account-details")
	@ResponseBody
	public ResponseEntity<Map<String,Object>>viewAccountDetails(@RequestParam("accno") String accNo,Model model){
		Map<String,Object> response = new HashMap<>();
		try (Connection con = datasrc.getConnection()){
			String query = "SELECT *FROM bank_acc WHERE acc_no = ?";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, accNo);
			ResultSet rs = ps.executeQuery();
			
					if(rs.next()) {
						
						response.put("success", true);
						response.put("accNo", rs.getString("acc_no"));
						response.put("accHolder", rs.getString("acc_holder"));
						response.put("acctype", rs.getString("acc_type"));
						response.put("aadharNo", rs.getDouble("aadhar_no"));
						response.put("Dob", rs.getDate("DOB"));
						response.put("balance", rs.getDouble("balance"));
					
					}
					else {
						response.put("success", false);
						response.put("errorMessage", "Invalid Account number");
					}
					
		}catch(Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("errorMessage","An error occurred while fetching account details.");
			
		}
		return ResponseEntity.ok(response);
     }
	
	
	
	@PostMapping("/validate-account-details")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> validateAccountDetails(@RequestParam("accnum") String accNum) {
	    Map<String, Object> response = new HashMap<>();
	    boolean isValid = validateAccount(accNum);

	    if (isValid) {
	        response.put("success", true);
	    } else {
	        response.put("success", false);
	        response.put("errorMessage", "Account not found. Please enter a valid account number.");
	    }

	    return ResponseEntity.ok(response);
	}

	private boolean validateAccount(String accNum) {
	    try (Connection con = datasrc.getConnection()) {
	        String query = "SELECT COUNT(*) FROM bank_acc WHERE acc_no = ?";
	        PreparedStatement ps = con.prepareStatement(query);
	        ps.setString(1, accNum);
	        ResultSet rs = ps.executeQuery();

	        if (rs.next() && rs.getInt(1) > 0) {
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	

	@PostMapping("/update-account-details")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> updateAccountDetails(@RequestParam("accnum") String accNum,
                                                                   @RequestParam("newName") String newName,
                                                                   @RequestParam("newAccType") String newAccType,
                                                                   @RequestParam("newDob") java.sql.Date newDob){
	Map<String,Object> response = new HashMap<>();
	
	try(Connection con = datasrc.getConnection()){
		String query = "UPDATE bank_acc SET acc_holder = ?,acc_type =?,DOB= ? WHERE acc_no =? ";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, newName);
		ps.setString(2, newAccType);
		ps.setDate(3, newDob);
		ps.setString(4,accNum);
		
		int updateRows = ps.executeUpdate();
		
		if(updateRows>0) {
			response.put("success", true);
		}
		else {
			response.put("success", false);
            response.put("errorMessage", "Account update failed. Please check the account number.");
        }
		
	}
	catch(Exception e) {
		 e.printStackTrace();
		 response.put("success", false);
	     response.put("errorMessage", "An error occurred while updating account details.");
	}
		
	   return ResponseEntity.ok(response);	
	}  
	
	
	
	
	
	
	@PostMapping("/validate-account-number")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> validateAccountNumber(@RequestParam("senderAccount") String senderAccount,
			                                                          @RequestParam("receiverAccount") String receiverAccount ,Model model,HttpSession session  ) {
	    Map<String, Object> response = new HashMap<>();
	    boolean isValid = validateAccountnum(senderAccount) && validateAccountnum(receiverAccount);
	   // System.out.println(senderAccount);

	    if (isValid) {
	    	 session.setAttribute("senderAccount", senderAccount);
	         session.setAttribute("receiverAccount", receiverAccount);
	        response.put("success", true);
	    } else {
	        response.put("success", false);
	        response.put("errorMessage", "Account not found. Please enter a valid account number.");
	    }

	    return ResponseEntity.ok(response);
	}
    
	private boolean validateAccountnum(String accNum) {
	    try (Connection con = datasrc.getConnection()) {
	        String query = "SELECT COUNT(*) FROM bank_acc WHERE acc_no = ? ";
	        PreparedStatement ps = con.prepareStatement(query);
	        ps.setString(1, accNum);
	       
	        ResultSet rs = ps.executeQuery();
	        
	       
	        if( rs.next()) {
	        	int count = rs.getInt(1);
	          return count>0;
	           }
	     
	    }catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	   

	@PostMapping("/transfer-money")
	@ResponseBody
	
	public ResponseEntity<Map<String,Object>> transferMoney(@RequestParam("transferAmount") double transferAmount,
			HttpSession session) throws SQLException{
		 Map<String,Object> response = new HashMap<>();
		
		  String senderAccount = (String) session.getAttribute("senderAccount");
		  String receiverAccount = (String) session.getAttribute("receiverAccount");
		// System.out.println(senderAccount);
		 double balance = getbalance(senderAccount);
		 //System.out.println(balance);
		 
		
			if (transferAmount >= balance) {
				response.put("success", false);
				response.put("errorMessage", "The amount entered is invalid.");
				
			}

			else  {
			boolean successfull = performMoneyTransfer(senderAccount,receiverAccount, transferAmount);
		
			if (successfull) {
				
				response.put("success", true);
				
		        response.put("newSenderBalance",getSenderBalance(senderAccount));
//		        System.out.println( getSenderBalance(senderAccount));
			} else {
				response.put("success", false);
				response.put("errorMessage", "Money transfer failed.");
			}

			}
			return ResponseEntity.ok(response);
		}

		private boolean performMoneyTransfer(String senderAccount,String receiverAccount ,double transferAmount) throws SQLException {
			 Connection connection = datasrc.getConnection();
			String query = "UPDATE bank_acc SET balance = balance - ? WHERE acc_no = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setDouble(1, transferAmount);
			ps.setString(2, senderAccount);
			int rowsAffected = ps.executeUpdate();

			
			String query2 = "UPDATE bank_acc SET balance = balance + ? WHERE acc_no = ?";
			PreparedStatement ps1 = connection.prepareStatement(query2);
			ps1.setDouble(1, transferAmount);
			ps1.setString(2, receiverAccount);
			int rowsAffected2 = ps1.executeUpdate();
			
			if (rowsAffected == 1 && rowsAffected2 ==1 ) {
				String transactionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String transactionType = "transfer";
				String query3 = "INSERT INTO transactions(SAcc_no, RAcc_no, Trans_Type, Trans_amnt,Tran_date) VALUES(?,?,?,?,?)";
				PreparedStatement ps2 = connection.prepareStatement(query3);
				ps2.setString(1, senderAccount);
				ps2.setString(2, receiverAccount);
				ps2.setString(3, transactionType);
				ps2.setDouble(4, transferAmount);
				ps2.setString(5, transactionDate);
				ps2.executeUpdate();
				
				
				return true;
			} else {
				
				return false;
			}
			 
		}
		private double getbalance(String accountNumber) throws SQLException {
			Connection connection = datasrc.getConnection();
			String query = "SELECT balance FROM bank_acc WHERE acc_no = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, accountNumber);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getDouble("balance");
			} else {
				return 0;
			}
		}

		

		private double getSenderBalance(String accountNumber) throws SQLException {
			Connection connection = datasrc.getConnection();
			String query = "SELECT balance FROM bank_acc WHERE acc_no = ?";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, accountNumber);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getDouble("balance");
			} else {
				return 0;
			}
		}
		
		@PostMapping("/validate-account-no")
		@ResponseBody
		public ResponseEntity<Map<String, Object>> validateAccountNo(@RequestParam("accNo") String accNo,
				                                                           Model model,HttpSession session  ) {
		    Map<String, Object> response = new HashMap<>();
		    boolean isValid = validateAccountno(accNo);
		   // System.out.println(senderAccount);

		    if (isValid) {
		    	 session.setAttribute("accNo", accNo);
		        // session.setAttribute("receiverAccount", receiverAccount);
		        response.put("success", true);
		    } else {
		        response.put("success", false);
		        response.put("errorMessage", "Account not found. Please enter a valid account number.");
		    }

		    return ResponseEntity.ok(response);
		}
	    
		private boolean validateAccountno(String accNum) {
		    try (Connection con = datasrc.getConnection()) {
		        String query = "SELECT COUNT(*) FROM bank_acc WHERE acc_no = ? ";
		        PreparedStatement ps = con.prepareStatement(query);
		        ps.setString(1, accNum);
		       
		        ResultSet rs = ps.executeQuery();
		        
		       
		        if( rs.next()) {
		        	int count = rs.getInt(1);
		          return count>0;
		           }
		     
		    }catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return false;
		}

		
		@PostMapping("/transactions")
		@ResponseBody
		public ResponseEntity<Map<String, Object>> getTransactions(HttpSession session) {
		    Map<String, Object> response = new HashMap<>();
		    List<Map<String, Object>> transactions = new ArrayList<>();
		    String accNo = (String) session.getAttribute("accNo");
		    
		    try (Connection connection = datasrc.getConnection()) {
		        String query = "SELECT * FROM transactions WHERE SAcc_no = ? OR RAcc_no = ?";
		        PreparedStatement ps = connection.prepareStatement(query);
		        ps.setString(1, accNo);
		        ps.setString(2, accNo);
		        ResultSet rs = ps.executeQuery();

		        while (rs.next()) {
		            Map<String, Object> transaction = new HashMap<>();
		            transaction.put("transId", rs.getInt("Trans_Id"));
		            transaction.put("sAccNo", rs.getString("SAcc_no"));
		            transaction.put("rAccNo", rs.getString("RAcc_no"));
		            transaction.put("transType", rs.getString("Trans_Type"));
		            transaction.put("transAmount", rs.getDouble("Trans_amnt"));
		            transaction.put("tranDate", rs.getDate("Tran_date"));
		            transactions.add(transaction);
		        }
		    } catch (SQLException e) {
		        throw new RuntimeException(e);
		    }

		    response.put("success", true);
		    response.put("transactions", transactions);
		    return ResponseEntity.ok(response);
		}
		
		
		@PostMapping("/validate-account-num")
		@ResponseBody
		public ResponseEntity<Map<String, Object>> validateAccountNum(@RequestParam("Accnum") String Accnum,
				                                                           Model model,HttpSession session  ) {
		    Map<String, Object> response = new HashMap<>();
		    boolean isValid = validateAccountnumber(Accnum);
		   // System.out.println(senderAccount);

		    if (isValid) {
		    	 session.setAttribute("Accnum", Accnum);
		        // session.setAttribute("receiverAccount", receiverAccount);
		        response.put("success", true);
		    } else {
		        response.put("success", false);
		        response.put("errorMessage", "Account not found. Please enter a valid account number.");
		    }

		    return ResponseEntity.ok(response);
		}
	    
		private boolean validateAccountnumber(String accNum) {
		    try (Connection con = datasrc.getConnection()) {
		        String query = "SELECT COUNT(*) FROM bank_acc WHERE acc_no = ? ";
		        PreparedStatement ps = con.prepareStatement(query);
		        ps.setString(1, accNum);
		       
		        ResultSet rs = ps.executeQuery();
		        
		       
		        if( rs.next()) {
		        	int count = rs.getInt(1);
		          return count>0;
		           }
		     
		    }catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return false;
		}
		
		@PostMapping("/calculate-interest")
		@ResponseBody
		public ResponseEntity<Map<String, Object>> interestCalc(HttpSession session) throws SQLException{
			 Map<String, Object> response = new HashMap<>();
			 String Accnum = (String) session.getAttribute("Accnum");
			 
			 double currentBalance = getbalance(Accnum);
			 double interestRate=0.05; //5%
			 
			 double interest = currentBalance * interestRate;
	            double newBalance = currentBalance + interest;
	            
	            response.put("success",true);
	            response.put("interest", interest);
	            response.put("newBalance", newBalance);

	            return ResponseEntity.ok(response);
	        } 
			 

		@PostMapping("/validate-account-No")
		@ResponseBody
		public ResponseEntity<Map<String, Object>> validateAccountnum(@RequestParam("accnum") String Accnum,
				                                                           Model model,HttpSession session  ) {
		    Map<String, Object> response = new HashMap<>();
		    boolean isValid = validateAccountnum2(Accnum);
		   // System.out.println(senderAccount);

		    if (isValid) {
		    	 session.setAttribute("accnum", Accnum);
		        // session.setAttribute("receiverAccount", receiverAccount);
		        response.put("success", true);
		    } else {
		        response.put("success", false);
		        response.put("errorMessage", "Account not found. Please enter a valid account number.");
		    }

		    return ResponseEntity.ok(response);
		}
	    
		private boolean validateAccountnum2(String accNum) {
		    try (Connection con = datasrc.getConnection()) {
		        String query = "SELECT COUNT(*) FROM bank_acc WHERE acc_no = ? ";
		        PreparedStatement ps = con.prepareStatement(query);
		        ps.setString(1, accNum);
		       
		        ResultSet rs = ps.executeQuery();
		        
		       
		        if( rs.next()) {
		        	int count = rs.getInt(1);
		          return count>0;
		           }
		     
		    }catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return false;
		}
		 @PostMapping("/generate-report")
		 @ResponseBody
			public ResponseEntity<Map<String, Object>> Transactionreport(HttpSession session) {
			    Map<String, Object> response = new HashMap<>();
			    List<Map<String, Object>> transactions = new ArrayList<>();
			    String accNo = (String) session.getAttribute("accnum");
			    
			    try (Connection connection = datasrc.getConnection()) {
			        String query = "SELECT * FROM transactions WHERE SAcc_no = ? OR RAcc_no = ?";
			        PreparedStatement ps = connection.prepareStatement(query);
			        ps.setString(1, accNo);
			        ps.setString(2, accNo);
			        ResultSet rs = ps.executeQuery();

			        while (rs.next()) {
			            Map<String, Object> transaction = new HashMap<>();
			            transaction.put("transId", rs.getInt("Trans_Id"));
			            transaction.put("sAccNo", rs.getString("SAcc_no"));
			            transaction.put("rAccNo", rs.getString("RAcc_no"));
			            transaction.put("transType", rs.getString("Trans_Type"));
			            transaction.put("transAmount", rs.getDouble("Trans_amnt"));
			            transaction.put("tranDate", rs.getDate("Tran_date"));
			            transactions.add(transaction);
			        }
			    } catch (SQLException e) {
			        throw new RuntimeException(e);
			    }

			    response.put("success", true);
			    response.put("transactions", transactions);
			    return ResponseEntity.ok(response);
			}
			
		
		
	}
	