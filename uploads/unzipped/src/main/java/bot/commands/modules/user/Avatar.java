package bot.commands.modules.user;

import bot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.User.Profile;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Avatar implements ICommand {

    private String name = "avatar";

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // get options
        User user = event.getOption("usuario").getAsUser();
        boolean banner = event.getOption("banner").getAsBoolean();
        // get avatar
        String avatar = user.getAvatarUrl() + "?size=1024";
        // get banner
        String bannerURL = "";
        if (banner) {
            Profile profile = user.retrieveProfile().complete();
            bannerURL = profile.getBannerUrl();
            bannerURL = bannerURL == null ? "No tiene banner" : bannerURL + "?size=2048";
        }
        // send response
        MessageEmbed avatarEmbed = buildAvatarEmbed(avatar, user.getAsTag());
        MessageEmbed bannerEmbed = buildBannerEmbed(bannerURL, user.getAsTag());

        event.replyEmbeds(avatarEmbed, bannerEmbed).queue();
    }

    private MessageEmbed buildAvatarEmbed(String avatar, String user) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Avatar de " + user)
                .setImage(avatar); // Añadir la imagen del avatar

        return embedBuilder.build();
    }

    private MessageEmbed buildBannerEmbed(String banner, String user) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Banner de " + user);

        if (!"No tiene banner".equals(banner)) {
            embedBuilder.setImage(banner); // Añadir la imagen del banner en tamaño completo
        } else {
            embedBuilder.setDescription("No tiene banner");
        }

        return embedBuilder.build();
    }

    @Override
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("avatar", "Responde con el avatar de un usuario")
                .addOption(OptionType.USER, "usuario", "Usuario del que obtener el avatar", false)
                .addOption(OptionType.BOOLEAN, "banner", "Obtener el banner del usuario", false);
        return slash;
    }

    @Override
    public String getName() {
        return name;
    }

}
