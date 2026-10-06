<i18n lang="yaml">
de:
  all: Alle
  selectToCreate: Wähle einen Zugangsdatentyp aus, um dessen Zugangsdaten zu erstellen
  search: Durchsuchen...
  create: Erstellen
  noCredentialTypeSelected: Wähle einen Zugangsdatentyp aus, um dessen Zugangsdaten zu verwalten.
  noCredentials:
    title: Noch keine Zugangsdaten verfügbar.
    description: Erstelle mit dem Knopf in der rechten oberen Ecke neue!
  scope:
    USER: Nutzer
    ROLE: Rolle
    GLOBAL: Global
en:
  all: Alle
  selectToCreate: Select a Credential Type to create a new entry
  search: Search...
  create: Create
  noCredentials:
    title: No Credentials available yet.
    description: Create new ones with the button in the top right corner!
  scope:
    USER: User
    ROLE: Role
    GLOBAL: Global
</i18n>

<script setup lang="ts">
import { useCredentials } from "~/composables/credential/useCredentials"
import { useAsyncData } from "#app"
import { useRouteQuery } from "@vueuse/router"
import CredentialCreateButton from "~/components/credential/CredentialCreateButton.vue"
import { CredentialDtoScopeEnum } from "~/api/backend/generated"
import CredentialGrid from "~/components/credential/CredentialGrid.vue"

useHead({
	title: "Credentials",
})

const { t } = useI18n({ useScope: "local" })

const selectedType = useRouteQuery<string | undefined>("type")

const { listCredentialOverview, searchCredentials, groupCredentials } = useCredentials()

const { data: credentialDefinitions, refresh: refreshOverview } = useAsyncData(listCredentialOverview)
const query = ref("")

const filteredCredentialOverview = computed(() => [
	{
		displayName: t("all"),
		type: undefined,
		count: undefined,
	},
	...(credentialDefinitions.value?.filter(it =>
		it.type == undefined || it.type === selectedType.value
		|| it.displayName.toLowerCase().includes(query.value.toLowerCase()),
	) ?? []),
])

const { data: page, refresh: refreshPage } = useAsyncData(
	refDefault(selectedType, "all"),
	() => searchCredentials(selectedType.value),
	{ deep: true },
)

const pageCredentials = computed(() => {
	const credentials = page.value?.filter(it =>
		it.name.toLowerCase().includes(query.value.toLowerCase()),
	)
	if (!credentials) return undefined

	return groupCredentials(credentials)
})

const refresh = () => Promise.all([
	refreshPage(),
	refreshOverview(),
])

const credentialCreateButton = useTemplateRef("credentialCreateButton")
</script>

<template>
	<UDashboardPanel id="credentials">
		<template #header>
			<UDashboardNavbar title="Credentials">
				<template #right>
					<CredentialCreateButton
						v-if="selectedType && credentialDefinitions"
						ref="credentialCreateButton"
						:key="selectedType"
						:credential-definition="credentialDefinitions.find(it => it.type == selectedType)!"
						@create="refresh"
					/>
					<UBadge
						v-else
						icon="i-lucide-info"
						:label="t('selectToCreate')"
						:variant="'ghost' as never"
					/>
				</template>
			</UDashboardNavbar>
		</template>

		<template #body>
			<div class="grid grid-cols-[25%_auto] size-full gap-6">
				<div class="max-w-2xl flex flex-col gap-4">
					<UInput
						v-model="query"
						icon="i-lucide-search"
						type="text"
						:placeholder="t('search')"
						:ui="{ trailing: 'pe-1' }"
					>
						<template v-if="query?.length" #trailing>
							<UButton
								color="neutral"
								variant="link"
								size="sm"
								icon="i-lucide-circle-x"
								@click="query = ''"
							/>
						</template>
					</UInput>
					<div class="flex flex-col gap-1">
						<UButton
							v-for="credential in filteredCredentialOverview ?? []"
							:key="credential.type"
							:active="selectedType === credential.type"
							variant="ghost"
							color="neutral"
							active-color="primary"
							active-variant="subtle"
							class="p-3 cursor-pointer flex justify-between"
							@click="selectedType = credential.type"
						>
							<span>{{ credential.displayName }}</span>
							<UBadge
								v-if="credential.count != undefined"
								:color="credential.count > 0 ? 'primary' : 'neutral'"
								variant="subtle"
							>
								{{ credential.count }}
							</UBadge>
						</UButton>
					</div>
				</div>
				<div v-if="credentialDefinitions && pageCredentials" class="space-y-4 overflow-y-auto">
					<div v-if="Object.entries(pageCredentials).length == 0">
						<UEmpty
							icon="i-lucide-ghost"
							:title="t('noCredentials.title')"
							:description="t('noCredentials.description')"
							variant="naked"
							:actions="[{ label: t('create'), icon: 'i-lucide-plus', onClick: () => credentialCreateButton?.open() }]"
						/>
					</div>
					<div
						v-for="scope in [CredentialDtoScopeEnum.User, CredentialDtoScopeEnum.Role, CredentialDtoScopeEnum.Global]
							.filter(it => pageCredentials![it])"
						:key="`scope-${scope}`"
						class="space-y-4"
					>
						<div class="space-y-1">
							<template v-if="scope === CredentialDtoScopeEnum.Role">
								<template v-for="(_, role) in pageCredentials[scope]" :key="`role-${role}`">
									<h4 class="text-lg text-highlighted">
										{{ t(`scope.${scope}`) }}:
										{{ role }}
									</h4>
									<USeparator class="pb-1" :ui="{ border: 'border-muted' }"/>
									<CredentialGrid
										v-model="pageCredentials[scope]![role]!"
										:credential-definitions="credentialDefinitions"
										@delete="refresh"
									/>
								</template>
							</template>
							<template v-else>
								<template v-if="scope != CredentialDtoScopeEnum.User">
									<h4 class="text-lg text-highlighted">
										{{ t(`scope.${scope}`) }}
									</h4>
									<USeparator class="pb-1" :ui="{ border: 'border-muted' }"/>
								</template>
								<CredentialGrid
									v-model="pageCredentials[scope]!"
									:credential-definitions="credentialDefinitions"
									@delete="refresh"
								/>
							</template>
						</div>
					</div>
				</div>
				<div v-else>
					<p class="text-center text-muted">{{ t('noCredentialTypeSelected') }}</p>
				</div>
			</div>
		</template>
	</UDashboardPanel>
</template>
