import exceptions.IncorrectAmountException;
import exceptions.NotEnoughMoneyException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankAccountTest {

    private BankAccount bankAccount;
    private OperationsHistoryPrinter transactionsHistoryPrinter;

    @Mock
    Printer printer;
    @Mock
    DateProvider dateProvider;

    @Before
    public void setUp() throws Exception {
        transactionsHistoryPrinter = new OperationsHistoryPrinter(printer);
        bankAccount = new BankAccount(dateProvider, transactionsHistoryPrinter);
    }

    @Test
    public void should_create_a_new_bank_account_with_no_money() throws Exception {
        assertThat(bankAccount.moneyStored()).isEqualTo(0);
    }

    @Test
    public void should_store_a_deposit_with_a_positive_amount_of_money() throws Exception {
        bankAccount.deposit(2);

        assertThat(bankAccount.moneyStored()).isEqualTo(2);
    }

    @Test
    public void should_store_the_total_of_two_deposit() throws Exception {
        bankAccount.deposit(2);
        bankAccount.deposit(3.5);

        assertThat(bankAccount.moneyStored()).isEqualTo(5.5);
    }

    @Test
    public void should_withdraw_an_not_empty_account() throws Exception {
        bankAccount.deposit(5);

        bankAccount.withdraw(3.3);

        assertThat(bankAccount.moneyStored()).isEqualTo(1.7);
    }

    @Test(expected = IncorrectAmountException.class)
    public void should_not_be_able_to_deposit_a_negative_amount_of_money() throws Exception {
        bankAccount.deposit(-2);
    }


    @Test(expected = IncorrectAmountException.class)
    public void should_not_be_able_to_withdraw_a_negative_amount_of_money() throws Exception {
        bankAccount.withdraw(-2);
    }

    @Test(expected = NotEnoughMoneyException.class)
    public void should_not_be_able_to_withdraw_more_money_tha_money_stored() throws Exception {
        bankAccount.deposit(2);

        bankAccount.withdraw(3);
    }

    @Test
    public void should_store_each_transaction_amount() throws Exception {
        bankAccount.deposit(5);
        bankAccount.withdraw(2);

        List<Double> expectedTransactions = Arrays.asList(5.0, 2.0);

        assertThat(bankAccount.allOperationsAmount()).isEqualTo(expectedTransactions);
    }

    @Test
    public void should_store_type_of_transaction() throws Exception {
        bankAccount.deposit(2);
        bankAccount.withdraw(1);
        bankAccount.deposit(3);

        List<OperationType> expectedTransactionsType = Arrays.asList(OperationType.DEPOSIT, OperationType.WITHDRAWAL, OperationType.DEPOSIT);

        assertThat(bankAccount.allOperationsType()).isEqualTo(expectedTransactionsType);
    }

    @Test
    public void should_store_the_date_of_transaction() throws Exception {
        when(dateProvider.todaysDateAsString()).thenReturn("26-07-2017");
        bankAccount.deposit(2);
        when(dateProvider.todaysDateAsString()).thenReturn("27-07-2017");
        bankAccount.withdraw(1);

        List<String> expectedTransactionsDate = Arrays.asList("26-07-2017", "27-07-2017");

        assertThat(bankAccount.allOperationsDate()).isEqualTo(expectedTransactionsDate);
    }

    @Test
    public void historyPrinter_always_print_header() throws Exception {
        bankAccount.printOperationsHistory();

        verify(printer).print("DATE | OPERATION | AMOUNT | BALANCE");
    }

    @Test
    public void historyPrinter_print_a_transaction_after_the_header() throws Exception {
        when(dateProvider.todaysDateAsString()).thenReturn("26-07-2017");
        bankAccount.deposit(5);

        bankAccount.printOperationsHistory();

        InOrder printerOrder = Mockito.inOrder(printer);
        printerOrder.verify(printer).print("DATE | OPERATION | AMOUNT | BALANCE");
        printerOrder.verify(printer).print("26-07-2017 | DEPOSIT | 5.0€ | 5.0€");
    }

    @Test
    public void historyPrinter_print_transactions_from_newest_to_oldest() throws Exception {
        when(dateProvider.todaysDateAsString()).thenReturn("26-07-2017");
        bankAccount.deposit(100);
        bankAccount.deposit(900);
        when(dateProvider.todaysDateAsString()).thenReturn("27-07-2017");
        bankAccount.withdraw(500);

        bankAccount.printOperationsHistory();

        InOrder printerOrder = Mockito.inOrder(printer);
        printerOrder.verify(printer).print("DATE | OPERATION | AMOUNT | BALANCE");
        printerOrder.verify(printer).print("27-07-2017 | WITHDRAWAL | 500.0€ | 500.0€");
        printerOrder.verify(printer).print("26-07-2017 | DEPOSIT | 900.0€ | 1000.0€");
        printerOrder.verify(printer).print("26-07-2017 | DEPOSIT | 100.0€ | 100.0€");
    }
}