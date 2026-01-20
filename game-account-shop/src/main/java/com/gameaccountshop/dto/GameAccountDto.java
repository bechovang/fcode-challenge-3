package com.gameaccountshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GameAccountDto {

    @NotBlank(message = "Vui lòng nhập rank")
    @Size(max = 50, message = "Rank không được vượt quá 50 ký tự")
    private String accountRank;

    @NotNull(message = "Vui lòng nhập giá bán")
    @Min(value = 2001, message = "Giá bán phải lớn hơn 2,000 VNĐ")
    private Long price;

    @NotBlank(message = "Vui lòng nhập mô tả")
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;

    @NotBlank(message = "Vui lòng nhập tên tài khoản game")
    @Size(max = 100, message = "Tên tài khoản không được vượt quá 100 ký tự")
    private String accountUsername;

    @NotBlank(message = "Vui lòng nhập mật khẩu tài khoản game")
    @Size(max = 100, message = "Mật khẩu không được vượt quá 100 ký tự")
    private String accountPassword;

    public String getAccountRank() { return accountRank; }
    public void setAccountRank(String accountRank) { this.accountRank = accountRank; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public String getAccountPassword() { return accountPassword; }
    public void setAccountPassword(String accountPassword) { this.accountPassword = accountPassword; }
}
