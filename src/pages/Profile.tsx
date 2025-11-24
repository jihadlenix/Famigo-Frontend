import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { User, Edit2, Save, Camera, Users, Coins, ChevronLeft, Heart, Sparkles } from 'lucide-react';

interface Wallet {
    id: string;
    balance: number;
}

interface Member {
    id: string;
    user_id: string;
    role: string;
    username: string;
    full_name: string;
    avatar_url: string;
}

interface Family {
    id: string;
    name: string;
    members: Member[];
}

interface UserProfile {
    id: string;
    email: string;
    username: string;
    full_name: string;
    profile_pic: string;
    bio: string;
    wallet: Wallet | null;
}

const Profile: React.FC = () => {
    const { userId } = useParams<{ userId: string }>();
    const navigate = useNavigate();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [user, setUser] = useState<UserProfile | null>(null);
    const [family, setFamily] = useState<Family | null>(null);
    const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
    const [isEditing, setIsEditing] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        full_name: '',
        bio: '',
        profile_pic: '',
    });

    useEffect(() => {
        fetchCurrentUser();
        fetchProfile();
        fetchFamily();
    }, [userId]);

    const fetchCurrentUser = async () => {
        try {
            const response = await client.get('/users/me');
            setCurrentUser(response.data);
        } catch (error) {
            console.error('Error fetching current user:', error);
        }
    };

    const fetchProfile = async () => {
        try {
            const endpoint = userId ? `/users/${userId}` : '/users/me';
            const response = await client.get(endpoint);
            setUser(response.data);
            setFormData({
                username: response.data.username || '',
                full_name: response.data.full_name || '',
                bio: response.data.bio || '',
                profile_pic: response.data.profile_pic || '',
            });
        } catch (error) {
            console.error('Error fetching profile:', error);
        }
    };

    const fetchFamily = async () => {
        try {
            const response = await client.get('/families/my');
            if (response.data && response.data.length > 0) {
                setFamily(response.data[0]);
            }
        } catch (error) {
            console.error('Error fetching family:', error);
        }
    };

    const handlePhotoClick = () => {
        if (isOwnProfile && fileInputRef.current) {
            fileInputRef.current.click();
        }
    };

    const handlePhotoChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setUploading(true);
        try {
            const reader = new FileReader();
            reader.onloadend = async () => {
                const base64 = reader.result as string;

                // Upload to backend
                await client.post('/users/me/photo', { photo: base64 });

                // Update local state
                setUser(prev => prev ? { ...prev, profile_pic: base64 } : null);
                setFormData(prev => ({ ...prev, profile_pic: base64 }));
            };
            reader.readAsDataURL(file);
        } catch (error) {
            console.error('Error uploading photo:', error);
        } finally {
            setUploading(false);
        }
    };

    const handleSave = async () => {
        try {
            const response = await client.patch('/users/me', formData);
            setUser(response.data);
            setIsEditing(false);
        } catch (error) {
            console.error('Error updating profile:', error);
        }
    };

    const isOwnProfile = !userId || (currentUser && currentUser.id === user?.id);

    if (!user) return (
        <div className="flex justify-center items-center h-screen bg-gradient-to-br from-orange-50 via-blue-50 to-purple-50">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
        </div>
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-orange-50 via-blue-50 to-purple-50 pb-20">
            {/* Decorative Elements */}
            <div className="fixed top-10 right-10 text-6xl animate-bounce opacity-20">⭐</div>
            <div className="fixed bottom-20 left-10 text-5xl animate-pulse opacity-20">🎉</div>
            <div className="fixed top-1/3 left-20 text-4xl opacity-10">✨</div>

            {/* Header / Cover Area */}
            <div className="relative h-64 bg-gradient-to-br from-orange-400 via-pink-500 to-purple-600 rounded-b-[2.5rem] shadow-2xl overflow-hidden">
                <div className="absolute inset-0 opacity-20 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>

                {/* Floating Emojis */}
                <div className="absolute top-5 left-10 text-3xl animate-bounce">🌟</div>
                <div className="absolute top-10 right-20 text-2xl animate-pulse">💫</div>
                <div className="absolute bottom-10 left-1/4 text-2xl opacity-50">🎈</div>

                {/* Navigation Back */}
                {userId && (
                    <button
                        onClick={() => navigate('/profile')}
                        className="absolute top-6 left-6 p-2 bg-white/30 backdrop-blur-md rounded-full text-white hover:bg-white/40 transition transform hover:scale-110"
                    >
                        <ChevronLeft className="w-6 h-6" />
                    </button>
                )}

                {/* Profile Info Centered */}
                <div className="absolute -bottom-16 left-0 right-0 flex flex-col items-center">
                    <div className="relative group">
                        <div className="w-32 h-32 rounded-full p-1 bg-gradient-to-br from-yellow-300 via-orange-400 to-pink-500 shadow-2xl animate-pulse">
                            <img
                                src={user.profile_pic || `https://ui-avatars.com/api/?name=${user.full_name}&background=random`}
                                alt="Profile"
                                className="w-full h-full rounded-full object-cover border-4 border-white"
                            />
                        </div>
                        {isOwnProfile && (
                            <>
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept="image/*"
                                    onChange={handlePhotoChange}
                                    className="hidden"
                                />
                                <button
                                    onClick={handlePhotoClick}
                                    disabled={uploading}
                                    className="absolute bottom-2 right-2 p-2.5 bg-gradient-to-br from-blue-500 to-purple-600 text-white rounded-full shadow-lg hover:shadow-xl transition transform hover:scale-110 disabled:opacity-50"
                                >
                                    {uploading ? (
                                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                    ) : (
                                        <Camera className="w-4 h-4" />
                                    )}
                                </button>
                            </>
                        )}
                    </div>

                    <div className="mt-3 text-center">
                        {(isEditing && isOwnProfile) ? (
                            <input
                                type="text"
                                value={formData.full_name}
                                onChange={(e) => setFormData({ ...formData, full_name: e.target.value })}
                                className="text-2xl font-bold text-gray-900 text-center bg-white/80 rounded-lg px-3 py-1 border-2 border-orange-300 focus:outline-none focus:border-orange-500"
                                placeholder="Full Name"
                            />
                        ) : (
                            <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                                {user.full_name} <Sparkles className="w-5 h-5 text-yellow-500" />
                            </h1>
                        )}
                        <p className="text-sm text-gray-600 font-medium">@{user.username}</p>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="mt-20 px-4 sm:px-6 max-w-3xl mx-auto space-y-6">

                {/* Action Buttons */}
                {isOwnProfile && (
                    <div className="flex justify-center">
                        <button
                            onClick={() => isEditing ? handleSave() : setIsEditing(true)}
                            className={`flex items-center px-6 py-3 rounded-full font-bold shadow-lg transition transform hover:scale-105 ${isEditing
                                ? 'bg-gradient-to-r from-green-400 to-emerald-500 text-white hover:shadow-green-300'
                                : 'bg-gradient-to-r from-orange-400 to-pink-500 text-white hover:shadow-orange-300'
                                }`}
                        >
                            {isEditing ? (
                                <> <Save className="w-5 h-5 mr-2" /> Save Changes ✨ </>
                            ) : (
                                <> <Edit2 className="w-5 h-5 mr-2" /> Edit Profile 🎨 </>
                            )}
                        </button>
                    </div>
                )}

                {/* Stats Cards */}
                <div className="grid grid-cols-2 gap-4">
                    {/* Wallet Card */}
                    <div className="bg-gradient-to-br from-yellow-400 via-orange-500 to-red-500 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden group hover:shadow-2xl hover:scale-105 transition-all duration-300">
                        <div className="absolute right-2 top-2 text-5xl opacity-20 group-hover:opacity-30 transition">💰</div>
                        <div className="absolute -right-4 -bottom-4 text-8xl opacity-10">🪙</div>
                        <div className="relative z-10">
                            <div className="flex items-center space-x-2 mb-2">
                                <div className="p-2 bg-white/30 rounded-xl backdrop-blur-sm">
                                    <Coins className="w-6 h-6" />
                                </div>
                                <span className="font-bold text-yellow-50">Wallet 🏆</span>
                            </div>
                            <div className="text-4xl font-black">{user.wallet?.balance || 0}</div>
                            <div className="text-sm text-yellow-100 mt-1 font-medium">Points Available</div>
                        </div>
                    </div>

                    {/* Family Card */}
                    <div className="bg-gradient-to-br from-blue-500 via-indigo-600 to-purple-700 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden group hover:shadow-2xl hover:scale-105 transition-all duration-300">
                        <div className="absolute right-2 top-2 text-5xl opacity-20 group-hover:opacity-30 transition">👨‍👩‍👧‍👦</div>
                        <div className="absolute -right-4 -bottom-4 text-8xl opacity-10">❤️</div>
                        <div className="relative z-10">
                            <div className="flex items-center space-x-2 mb-2">
                                <div className="p-2 bg-white/30 rounded-xl backdrop-blur-sm">
                                    <Users className="w-6 h-6" />
                                </div>
                                <span className="font-bold text-blue-50">Family 💙</span>
                            </div>
                            <div className="text-2xl font-black truncate">{family?.name || 'No Family'}</div>
                            <div className="text-sm text-blue-100 mt-1 font-medium">{family?.members.length || 0} Members</div>
                        </div>
                    </div>
                </div>

                {/* Bio Section */}
                <div className="bg-white rounded-3xl p-6 shadow-lg border-2 border-orange-100 hover:shadow-xl transition-shadow">
                    <h3 className="text-xl font-black text-gray-800 mb-4 flex items-center">
                        <div className="p-2 bg-gradient-to-br from-purple-400 to-pink-500 rounded-xl mr-3">
                            <User className="w-5 h-5 text-white" />
                        </div>
                        About Me ✨
                    </h3>
                    {(isEditing && isOwnProfile) ? (
                        <textarea
                            value={formData.bio}
                            onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                            className="w-full p-4 bg-gradient-to-br from-orange-50 to-pink-50 border-2 border-orange-200 rounded-2xl focus:ring-2 focus:ring-orange-400 focus:border-transparent transition outline-none resize-none"
                            rows={4}
                            placeholder="Write something fun about yourself! 🎉"
                        />
                    ) : (
                        <p className="text-gray-700 leading-relaxed text-lg">
                            {user.bio || <span className="text-gray-400 italic">{isOwnProfile ? 'No bio yet. Click edit to add one! 📝' : 'No bio added yet.'}</span>}
                        </p>
                    )}
                </div>

                {/* Family Members List */}
                {family && (
                    <div className="bg-white rounded-3xl p-6 shadow-lg border-2 border-blue-100 hover:shadow-xl transition-shadow">
                        <h3 className="text-xl font-black text-gray-800 mb-5 flex items-center">
                            <div className="p-2 bg-gradient-to-br from-blue-400 to-purple-500 rounded-xl mr-3">
                                <Heart className="w-5 h-5 text-white" />
                            </div>
                            Family Members 👨‍👩‍👧‍👦
                        </h3>
                        <div className="space-y-3">
                            {family.members.map((member, index) => {
                                const isParent = member.role === 'PARENT';
                                const colors = [
                                    'from-purple-400 to-pink-500',
                                    'from-blue-400 to-cyan-500',
                                    'from-green-400 to-emerald-500',
                                    'from-orange-400 to-red-500',
                                ];
                                const emojis = ['👑', '⭐', '🌟', '💫'];

                                return (
                                    <div
                                        key={member.id}
                                        onClick={() => navigate(`/profile/${member.user_id}`)}
                                        className="flex items-center justify-between p-4 rounded-2xl hover:bg-gradient-to-r hover:from-orange-50 hover:to-pink-50 cursor-pointer transition-all border-2 border-transparent hover:border-orange-200 group transform hover:scale-102"
                                    >
                                        <div className="flex items-center space-x-4">
                                            <div className={`w-14 h-14 rounded-full bg-gradient-to-br ${colors[index % colors.length]} p-0.5 border-2 border-white shadow-lg group-hover:shadow-xl transition`}>
                                                <img
                                                    src={`https://ui-avatars.com/api/?name=${member.full_name || member.username}&background=random`}
                                                    alt={member.username}
                                                    className="w-full h-full rounded-full object-cover"
                                                />
                                            </div>
                                            <div>
                                                <p className="font-bold text-gray-900 text-lg flex items-center gap-2">
                                                    {member.full_name || member.username}
                                                    <span className="text-xl">{emojis[index % emojis.length]}</span>
                                                </p>
                                                <span className={`text-xs px-3 py-1 rounded-full font-bold ${isParent
                                                    ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white'
                                                    : 'bg-gradient-to-r from-blue-500 to-cyan-500 text-white'
                                                    }`}>
                                                    {isParent ? '👑 ' : '⭐ '}{member.role}
                                                </span>
                                            </div>
                                        </div>
                                        <ChevronLeft className="w-6 h-6 text-gray-300 rotate-180 group-hover:text-orange-500 transition transform group-hover:translate-x-1" />
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;
