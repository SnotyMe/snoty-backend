import { useApi } from "~/api/backend/api-clients"
import {
	CredentialApi,
	type CredentialCreateDto,
	type CredentialCreateDtoScopeEnum,
	type CredentialDto,
	type CredentialDtoScopeEnum,
	type PotentiallyAccessibleCredentialDto,
} from "~/api/backend/generated"

export const useCredentials = () => {
	const credentialApi = useApi(CredentialApi)

	const createCredential = (credential: CredentialCreateDto) =>
		credentialApi.wiringCredentialPost({ credentialCreateDto: credential })

	const getCredential = (credentialId: string) =>
		credentialApi.wiringCredentialIdGet({ id: credentialId })

	const listCredentialOverview = () =>
		credentialApi.wiringCredentialOverviewGet()

	const searchCredentials = (type: string | undefined): Promise<PotentiallyAccessibleCredentialDto[]> =>
		credentialApi.wiringCredentialSearchGet({ type }) as Promise<PotentiallyAccessibleCredentialDto[]>

	const groupCredentials = (credentials: PotentiallyAccessibleCredentialDto[]) => {
		const groupedByScope
			= Object.groupBy(credentials, it => it.scope)

		const result: Partial<Record<
			// @ts-expect-error TS2702 idiot language
			CredentialDtoScopeEnum.User | CredentialCreateDtoScopeEnum.Global,
			PotentiallyAccessibleCredentialDto[] | undefined
		>>
		& Partial<{
			[CredentialDtoScopeEnum.Role]: Record<string, PotentiallyAccessibleCredentialDto[] | undefined>
		}> = {
			...groupedByScope,
			ROLE: undefined,
		}

		const groupedByRole: Record<string, PotentiallyAccessibleCredentialDto[] | undefined> = Object.groupBy(
			groupedByScope.ROLE ?? [],
			it => it.requiredRole?.name ?? "unknown",
		)
		if ((groupedByScope.ROLE?.length ?? 0) > 0) {
			result.ROLE = groupedByRole
		} else {
			delete result["ROLE"]
		}

		return result
	}

	const enumerateCredentials = (credentialType: string) =>
		credentialApi.wiringCredentialCredentialTypeEnumerateGet({ credentialType })

	const updateCredential = (credentialUpdateDto: CredentialDto) =>
		credentialApi.wiringCredentialIdPut({ id: credentialUpdateDto.id, credentialUpdateDto })

	const deleteCredential = (credential: PotentiallyAccessibleCredentialDto) =>
		credentialApi.wiringCredentialIdDelete({ id: credential.id })

	return {
		createCredential,
		getCredential,
		listCredentialOverview,
		searchCredentials,
		groupCredentials,
		enumerateCredentials,
		updateCredential,
		deleteCredential,
	}
}
