package main.java;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.*;
import java.text.NumberFormat;

public class BotCommands extends ListenerAdapter {

    private Map<String, ScheduledAlert> scheduledAlertMap = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("test")) {
            event.reply("test").queue();


        } else if (command.equals("crypto-price")) {
            OptionMapping cryptoOption = event.getOption("crypto-symbol");
            String cryptoSymbolDiscord = cryptoOption.getAsString().toUpperCase();

            System.out.println(cryptoSymbolDiscord);

            CryptoPrice cmcApi = new CryptoPrice(cryptoSymbolDiscord);
            double price = cmcApi.getPrice(cryptoSymbolDiscord);

            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            String priceString = formatter.format(price);
            event.reply("Request sent!").setEphemeral(false).queue();
            event.getChannel().sendMessage("The current price of " + cryptoSymbolDiscord + " is " + priceString).queue();

        } else if (command.equals("bitcoin-alert-start")) {
            BitcoinPriceAlert alert = new BitcoinPriceAlert();
            alert.startAlerts(event.getTextChannel());
            event.reply("Alert created!").queue();

        } else if (command.equals("bitcoin-alert-stop")) {
            BitcoinPriceAlert alert = new BitcoinPriceAlert();
            alert.stopAlerts(event.getTextChannel());
            event.reply("Alert disabled!").queue();

        } else if (command.equals("bitcoin-scheduled-alert-start")) {
            ScheduledAlert scheduledAlert = new ScheduledAlert(event.getTextChannel());
            scheduledAlert.start(LocalTime.of(15, 19, 30));
            scheduledAlertMap.put(event.getTextChannel().getId(), scheduledAlert);
            event.reply("The daily closing price of Bitcoin will be displayed from now on!").queue();

        } else if (command.equals("bitcoin-scheduled-alert-stop")) {
            ScheduledAlert scheduledAlert = scheduledAlertMap.get(event.getTextChannel().getId());
            if (scheduledAlert != null) {
                scheduledAlert.stop();
                scheduledAlertMap.remove(event.getTextChannel().getId());
                event.reply("The current scheduled alert has been stopped!").queue();
            }
        }
    }
    //Registers the commands
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        //Test
        commandData.add(Commands.slash("test", "testing"));

        //say <message>
        OptionData cryptoTag = new OptionData(OptionType.STRING, "crypto-symbol", "Enter the symbol of the crypto you want the price of", true);
        commandData.add(Commands.slash("crypto-price", "Get the price of a crypto").addOptions(cryptoTag));

        commandData.add(Commands.slash("bitcoin-alert-start", "Create a tracker for Bitcoin"));
        commandData.add(Commands.slash("bitcoin-alert-stop", "Disable previous tracker for Bitcoin"));

        commandData.add(Commands.slash("bitcoin-scheduled-alert-start", "Send the price of Bitcoin at 9:00 PM BRT everyday"));
        commandData.add(Commands.slash("bitcoin-scheduled-alert-stop", "Disable the scheduled alert"));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}

