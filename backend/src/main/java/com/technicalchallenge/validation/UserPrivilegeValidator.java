package com.technicalchallenge.validation;

import org.springframework.stereotype.Service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.ApplicationUser;
import com.technicalchallenge.repository.ApplicationUserRepository;

@Service
public class UserPrivilegeValidator {

  private final ApplicationUserRepository applicationUserRepository;

  public UserPrivilegeValidator(ApplicationUserRepository applicationUserRepository) {
    this.applicationUserRepository = applicationUserRepository;
  }
}
  /**
   * Validates whether a user is authorised to perform a given operation on a trade
   */

   // TEMPORARY: bypass privilege check due to bug
   // TO DO: fix UserPrivilegeRepository and proper privilege check
  public boolean validateUserPrivileges(String userId, String operation, TradeDTO tradeDTO) {

    return true;

    // Basic null safety - Protects against null pointers on user, user profile, or trade data
    // if (userId == null || operation == null || tradeDTO == null) {
    //   return false;
    // }

    // Looks user up
    // ApplicationUser user = applicationUserRepository.findByLoginId(userId).orElse(null);

    // Basic null safety - protects against null pointers on user, user profile, or trade data
    // if (user == null || user.getUserProfile() == null || user.getUserProfile().getUserType() == null) {
    //   return false;
    // }

    // Both role and operation are normaliSed to uppercase, so you can safely match any format
    // String userType = user.getUserProfile().getUserType().trim().toUpperCase();
    // String normalisedOperation = operation.trim().toUpperCase();

    // switch (userType) {
      
    // case "SUPERUSER":
        // Full system access
        // return true;


      // case "TRADER_SALES":
        // Full access only to their own trades
      //   return tradeDTO.getTraderUserName() != null &&
      //   tradeDTO.getTraderUserName().equalsIgnoreCase(userId);

      // case "MO":
      // case "MIDDLE_OFFICE":
        // Can amend and view
      //   return normalisedOperation.equals("AMEND") || normalisedOperation.equals("VIEW");

      // case "SUPPORT":
        // Can only view
      //   return normalisedOperation.equals("VIEW");

//       default:
//       Unrecognised role
//         return false;
//     }
//   }
}
