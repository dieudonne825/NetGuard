package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NetAlertRed
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetWarningAmber

enum class LedState(val label: String, val color: Color, val isBlinking: Boolean = false) {
    OFF("Éteint", NetTextMuted),
    GREEN("Vert Fixe", NetOnlineGreen),
    GREEN_BLINKING("Vert Clignotant", NetOnlineGreen, isBlinking = true),
    AMBER("Orange / Ambre", NetWarningAmber),
    RED("Rouge Fixe", NetAlertRed),
    RED_BLINKING("Rouge Clignotant", NetAlertRed, isBlinking = true)
}

data class LedIndicatorInfo(
    val name: String,
    val state: LedState,
    val description: String,
    val diagnosticHelp: String
)

data class RouterHardwareModel(
    val id: String,
    val name: String,
    val provider: String,
    val type: String, // "Fibre Wi-Fi 6E", "Fibre 10G", "4G/5G Router"
    val powerLed: LedIndicatorInfo,
    val wanLed: LedIndicatorInfo,
    val internetLed: LedIndicatorInfo,
    val wifiLed: LedIndicatorInfo,
    val lanLed: LedIndicatorInfo
) {
    companion object {
        fun createModelForFault(
            modelId: String,
            faultType: NetworkFaultType
        ): RouterHardwareModel {
            val baseName = when(modelId) {
                "livebox" -> "Livebox 6 / 7 Fibre"
                "freebox" -> "Freebox Ultra 10G"
                "bbox" -> "Bbox Wi-Fi 6"
                "sfrbox" -> "SFR Box 8 Fibre"
                else -> "NetGuard Pro Gateway"
            }
            val provider = when(modelId) {
                "livebox" -> "Orange"
                "freebox" -> "Free"
                "bbox" -> "Bouygues Telecom"
                "sfrbox" -> "SFR"
                else -> "NetGuard Tech"
            }

            return when (faultType) {
                NetworkFaultType.NONE_ONLINE -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation stable 12V", "Votre modem est correctement alimenté."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.GREEN, "Signal optique à -18.2 dBm OK", "Liaison optique avec le central FAI opérationnelle."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.GREEN_BLINKING, "Trafic de données actif", "L'accès Internet est fluide."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.GREEN, "Bandes 2.4G / 5G / 6G actives", "Point d'accès Wi-Fi opérationnel."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "Ports Ethernet 1Gb/10Gb", "Liaison réseau filaire active.")
                )
                NetworkFaultType.ISP_OUTAGE -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK (Local)", "Le modem est sous tension."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.RED_BLINKING, "Perte de Synchro Optique FAI", "Problème sur le réseau externe de votre opérateur (Raccordement ou NRO)."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.RED, "Pas de connexion WAN Internet", "Pas d'adresse IP publique attribuée par le FAI."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.GREEN, "Réseau Wi-Fi local actif", "Le Wi-Fi fonctionne en local mais sans accès web."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "Ethernet actif", "Connexion réseau locale uniquement.")
                )
                NetworkFaultType.POWER_OUTAGE -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.RED, "Problème Alimentation / Éteint", "Coupure de courant secteur 230V ou bloc d'alimentation débranché."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.OFF, "Éteint (Aucun courant)", "Module optique non alimenté."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.OFF, "Éteint", "Modem hors service par manque d'électricité."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.OFF, "Réseau Wi-Fi éteint", "Le point d'accès ne diffuse plus."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.OFF, "Ports hors tension", "Tous les voyants sont éteints.")
                )
                NetworkFaultType.FIBER_SIGNAL_LOW -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK", "Alimentation stabilisée."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.AMBER, "Signal Optique Dégradé (-28.5 dBm)", "Puissance optique trop faible. Nettoyez le connecteur SC/APC ou détordez la jarretière."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.AMBER, "Débit Troncqué / Pertes", "Accès Web fortement ralenti avec déconnexions régulières."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.GREEN, "Wi-Fi Actif", "Diffusion sans-fil fonctionnelle."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "LAN Actif", "Réseau local OK.")
                )
                NetworkFaultType.DNS_SERVER_DOWN -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK", "Bloc secteur OK."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.GREEN, "Signal Optique OK (-19 dBm)", "Lien physique Fibre parfaitement synchronisé."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.RED, "Échec Résolution DNS FAI", "L'IP publique est obtenue, mais les serveurs DNS de l'opérateur ne répondent pas."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.GREEN, "Wi-Fi Actif", "Connexion sans-fil établie."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "LAN Actif", "Liaison locale OK.")
                )
                NetworkFaultType.WIFI_INTERFERENCE -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK", "Alimentation stable."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.GREEN, "Lien Optique OK", "Réseau FAI opérationnel."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.GREEN, "Accès Filaire OK", "Connexion web normale en Ethernet."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.RED_BLINKING, "Brouillage / Canaux Saturation", "Fortes interférences hertziennes sur 2.4/5GHz. Changez de canal Wi-Fi."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "Ethernet OK", "Connexion filaire recommandée pendant l'incident.")
                )
                NetworkFaultType.DHCP_POOL_EXHAUSTED -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK", "Box sous tension."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.GREEN, "Lien Optique OK", "Fibre opérationnelle."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.GREEN, "Internet OK", "Connexion générale valide."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.AMBER, "Connexion Wi-Fi Refusée (DHCP)", "Saturations d'adresses IP locales. Aucun nouvel équipement ne peut se connecter."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.AMBER, "Conflit IP Filaire", "Assigne manuellement une IP fixe aux équipements.")
                )
                NetworkFaultType.MODEM_OVERHEAT -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.RED_BLINKING, "ALERTE SURCHAUFFE CPU (88°C)", "Processeur en surchauffe critique ! Risque de dommage matériel."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.AMBER, "Ventilation Ineffective", "Sécurité thermique engagée."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.AMBER, "Mode Sécurité Débridé", "Services réduits pour diminuer la charge processeur."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.OFF, "Wi-Fi Désactivé (Sécurité)", "Réseau sans-fil coupé automatiquement pour refroidir le chipset."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.GREEN, "LAN Restreint", "Ethernet maintenu sous surveillance.")
                )
                NetworkFaultType.ETHERNET_CABLE_FAULT -> RouterHardwareModel(
                    id = modelId,
                    name = baseName,
                    provider = provider,
                    type = "Fibre Multi-Gig",
                    powerLed = LedIndicatorInfo("POWER / ALIM", LedState.GREEN, "Alimentation OK", "Alimentation stable."),
                    wanLed = LedIndicatorInfo("WAN / FIBRE", LedState.GREEN, "Signal Optique OK", "Fibre 10G connectée."),
                    internetLed = LedIndicatorInfo("INTERNET", LedState.GREEN, "Internet OK", "Service Web en ligne."),
                    wifiLed = LedIndicatorInfo("WI-FI 6 GHz", LedState.GREEN, "Wi-Fi OK", "Sans-fil nominal."),
                    lanLed = LedIndicatorInfo("LAN 1-4", LedState.AMBER, "Port LAN Bridé à 10 Mbps", "Mauvais contact ou câble RJ45 pincé. Débit filaire chuté à 10 Mbps.")
                )
            }
        }
    }
}

enum class NetworkFaultType(
    val title: String,
    val badgeLabel: String,
    val category: String,
    val severity: String,
    val description: String,
    val recommendedFix: String
) {
    NONE_ONLINE(
        title = "Réseau Opérationnel",
        badgeLabel = "EN LIGNE",
        category = "Ligne Fibre OK",
        severity = "NORMAL",
        description = "Tous les services (Optique, IP WAN, Wi-Fi 6, LAN) fonctionnent de manière optimale.",
        recommendedFix = "Aucune action requise. Votre modem fonctionne parfaitement."
    ),
    ISP_OUTAGE(
        title = "Rupture Fibre / Panne FAI",
        badgeLabel = "PANNE FAI",
        category = "Réseau Optique WAN",
        severity = "CRITIQUE",
        description = "Signal laser optique interrompu sur le réseau externe de votre opérateur (NRO / PBO déconnecté).",
        recommendedFix = "Signalement automatique ouvert vers votre opérateur. Un technicien Fibre doit intervenir."
    ),
    POWER_OUTAGE(
        title = "Coupure d'Alimentation",
        badgeLabel = "PANNE ALIM",
        category = "Alimentation 12V",
        severity = "CRITIQUE",
        description = "Le modem ne reçoit plus de tension électrique (transformateur secteur débranché ou disjoncteur sauté).",
        recommendedFix = "Vérifiez le branchement du bloc secteur sur la prise mural 230V et rallumez le bouton ON/OFF."
    ),
    FIBER_SIGNAL_LOW(
        title = "Signal Optique Faible (-28.5 dBm)",
        badgeLabel = "ATTÉNUATION",
        category = "Réseau Optique WAN",
        severity = "MAJEUR",
        description = "Perte de puissance optique élevée (poussière sur le connecteur SC/APC ou pliure sur la jarretière verte).",
        recommendedFix = "Débranchez délicatement la jarretière optique verte, nettoyez l'embout avec un chiffon sec et rebranchez sans la plier."
    ),
    DNS_SERVER_DOWN(
        title = "Panne des Serveurs DNS FAI",
        badgeLabel = "PANNE DNS",
        category = "Services IP / Routage",
        severity = "MAJEUR",
        description = "La connexion physique est active mais les serveurs DNS de l'opérateur ne répondent plus aux requêtes web.",
        recommendedFix = "Changez temporairement les serveurs DNS dans votre modem par 1.1.1.1 (Cloudflare) ou 8.8.8.8 (Google)."
    ),
    WIFI_INTERFERENCE(
        title = "Interférences / Saturation Wi-Fi",
        badgeLabel = "BROUILLAGE WI-FI",
        category = "Réseau Sans-Fil",
        severity = "MOYEN",
        description = "Brouillage radio important sur les canaux 2.4 GHz et 5 GHz provoqué par les box voisines.",
        recommendedFix = "Dans l'onglet Wi-Fi, activez le 'Changement Automatique de Canal' ou basculez sur la bande 6 GHz (Wi-Fi 6E)."
    ),
    DHCP_POOL_EXHAUSTED(
        title = "Saturation Plage DHCP / Conflit IP",
        badgeLabel = "PLAGE DHCP PLEINE",
        category = "Réseau Local LAN",
        severity = "MOYEN",
        description = "Toutes les adresses IP locales (192.168.1.10 à 254) sont déjà attribuées aux appareils de la maison.",
        recommendedFix = "Élargissez la plage DHCP dans l'admin modem ou déconnectez les anciens équipements inactifs."
    ),
    MODEM_OVERHEAT(
        title = "Surchauffe Processeur (88°C)",
        badgeLabel = "SURCHAUFFE",
        category = "Matériel / Ventilation",
        severity = "CRITIQUE",
        description = "Surchauffe critique du chipset réseau (88°C). La box risque de redémarrer en urgence.",
        recommendedFix = "Dégagez immédiatement les grilles d'aération du modem. Ne posez aucun objet au-dessus et éloignez-le d'une source de chaleur."
    ),
    ETHERNET_CABLE_FAULT(
        title = "Câble Ethernet LAN Endommagé",
        badgeLabel = "CÂBLE DÉGRADÉ",
        category = "Connectique Filaire",
        severity = "MOYEN",
        description = "Lien Ethernet bridé à 10 Mbps au lieu de 1000 Mbps en raison d'un câble RJ45 pincé ou défectueux.",
        recommendedFix = "Remplacez le câble RJ45 reliant votre ordinateur ou décodeur TV par un câble Cat 6 ou Cat 7 neuf."
    )
}
